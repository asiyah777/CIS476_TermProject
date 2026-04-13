package com.driveshare.controller;

import com.driveshare.dto.BookingRequest;
import com.driveshare.model.Booking;
import com.driveshare.model.Car;
import com.driveshare.patterns.mediator.UIMediator;
import com.driveshare.patterns.mediator.BookingComponent;
import com.driveshare.repository.BookingRepository;
import com.driveshare.repository.CarRepository;
import com.driveshare.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController extends BaseController {

    @Autowired private BookingRepository   bookingRepository;
    @Autowired private CarRepository       carRepository;
    @Autowired private NotificationService notificationService;

    /** All bookings made by the current user (renter view). */
    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings(
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        Long userId = resolveUserId(token);
        List<Booking> mine = bookingRepository.findAll().stream()
                .filter(b -> userId.equals(b.getUserId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(mine);
    }

    /** All booking requests for cars owned by the current user (owner/host view). */
    @GetMapping("/owner")
    public ResponseEntity<List<Booking>> getOwnerBookings(
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        Long ownerId = resolveUserId(token);
        List<Long> myCarIds = carRepository.findAll().stream()
                .filter(c -> ownerId.equals(c.getOwnerId()))
                .map(Car::getId)
                .collect(Collectors.toList());
        List<Booking> ownerBookings = bookingRepository.findAll().stream()
                .filter(b -> myCarIds.contains(b.getCarId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ownerBookings);
    }

    /** Confirmed bookings for a specific car (used by booking modal to show unavailable dates). */
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<Booking>> getBookingsForCar(@PathVariable Long carId) {
        List<Booking> confirmed = bookingRepository.findByCarId(carId).stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(confirmed);
    }

    /** Renter submits a booking request. Starts as PENDING. */
    @PostMapping
    public ResponseEntity<String> createBooking(
            @RequestHeader(value = "X-Session-Token", required = false) String token,
            @RequestBody BookingRequest request) {

        String start = request.getStartDate() != null ? request.getStartDate() : request.getBookingDate();
        String end   = request.getEndDate();

        Car car = request.getCarId() != null
                ? carRepository.findById(request.getCarId()).orElse(null) : null;

        if (car != null && start != null && end != null) {
            LocalDate reqStart = LocalDate.parse(start);
            LocalDate reqEnd   = LocalDate.parse(end);

            if (car.getAvailableFrom() != null && reqStart.isBefore(LocalDate.parse(car.getAvailableFrom()))) {
                return ResponseEntity.badRequest().body(
                    "Start date is before the car's availability window (" + car.getAvailableFrom() + ").");
            }
            if (car.getAvailableTo() != null && reqEnd.isAfter(LocalDate.parse(car.getAvailableTo()))) {
                return ResponseEntity.badRequest().body(
                    "End date is after the car's availability window (" + car.getAvailableTo() + ").");
            }

            List<Booking> confirmed = bookingRepository.findByCarId(request.getCarId()).stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .collect(Collectors.toList());
            for (Booking b : confirmed) {
                if (b.getStartDate() != null && b.getEndDate() != null) {
                    LocalDate bStart = LocalDate.parse(b.getStartDate());
                    LocalDate bEnd   = LocalDate.parse(b.getEndDate());
                    if (!(reqEnd.compareTo(bStart) <= 0 || reqStart.compareTo(bEnd) >= 0)) {
                        return ResponseEntity.badRequest().body(
                            "Car is already booked from " + b.getStartDate() + " to " + b.getEndDate() + ".");
                    }
                }
            }
        }

        Booking booking = new Booking();
        booking.setCarId(request.getCarId());
        booking.setUserId(resolveUserId(token));
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setBookingDate(start);
        booking.setStatus("PENDING");

        if (car != null && start != null && end != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.parse(start), LocalDate.parse(end));
            booking.setTotalCost(car.getPricePerDay() * Math.max(1, days));
        }

        bookingRepository.save(booking);

        if (car != null) {
            String carName = car.getMake() + " " + car.getModel();
            String dates   = start + " to " + (end != null ? end : start);

            // Mediator pattern — coordinate booking and notification components
            UIMediator mediator = new UIMediator(notificationService);
            BookingComponent bookingComponent = new BookingComponent(mediator);
            bookingComponent.book("Request submitted for " + carName + " (" + dates + ")");
            mediator.processBookingRequest(booking, car, dates);
        }

        return ResponseEntity.ok("Booking request submitted. Awaiting owner confirmation.");
    }

    /** Owner confirms a pending booking request. */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<String> confirmBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.notFound().build();
        if (!"PENDING".equals(booking.getStatus())) {
            return ResponseEntity.badRequest().body("Booking is not in PENDING state.");
        }
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        Car car = booking.getCarId() != null
                ? carRepository.findById(booking.getCarId()).orElse(null) : null;
        String carName = car != null ? car.getMake() + " " + car.getModel() : "Car #" + booking.getCarId();
        String confirmMsg = "Booking confirmed for " + carName + " (" + booking.getStartDate()
            + " to " + booking.getEndDate() + "). Total: $"
            + String.format("%.2f", booking.getTotalCost()) + ". Payment is now due.";

        // Mediator pattern — coordinate booking confirmation and notification
        UIMediator mediator = new UIMediator(notificationService);
        BookingComponent bookingComponent = new BookingComponent(mediator);
        bookingComponent.book("Confirmed booking #" + booking.getId());
        mediator.processBookingConfirmation(booking, car);

        return ResponseEntity.ok("Booking confirmed.");
    }

    /** Owner denies a pending booking request. */
    @PutMapping("/{id}/deny")
    public ResponseEntity<String> denyBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.notFound().build();
        if (!"PENDING".equals(booking.getStatus())) {
            return ResponseEntity.badRequest().body("Booking is not in PENDING state.");
        }
        booking.setStatus("DENIED");
        bookingRepository.save(booking);

        Car car = booking.getCarId() != null
                ? carRepository.findById(booking.getCarId()).orElse(null) : null;
        String carName = car != null ? car.getMake() + " " + car.getModel() : "Car #" + booking.getCarId();

        // Mediator pattern — coordinate booking denial and notification
        UIMediator mediator = new UIMediator(notificationService);
        BookingComponent bookingComponent = new BookingComponent(mediator);
        bookingComponent.book("Denied booking #" + booking.getId());
        mediator.processBookingDenial(booking, car);

        return ResponseEntity.ok("Booking denied.");
    }
}
