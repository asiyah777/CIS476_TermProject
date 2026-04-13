package com.driveshare.controller;

import com.driveshare.model.Booking;
import com.driveshare.repository.BookingRepository;
import com.driveshare.repository.CarRepository;
import com.driveshare.service.NotificationService;
import com.driveshare.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
public class PaymentController extends BaseController {

    @Autowired private PaymentService      paymentService;
    @Autowired private BookingRepository   bookingRepository;
    @Autowired private CarRepository       carRepository;
    @Autowired private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<String> pay(
            @RequestHeader(value = "X-Session-Token", required = false) String token,
            @RequestBody Map<String, Object> req) {

        double amount = Double.parseDouble(req.get("amount").toString());
        Long   userId = resolveUserId(token);

        paymentService.makePayment(amount, userId);

        // Mark the current user's CONFIRMED unpaid bookings as paid
        List<Booking> toPay = bookingRepository.findAll().stream()
                .filter(b -> userId.equals(b.getUserId())
                          && "CONFIRMED".equals(b.getStatus())
                          && !b.isPaid())
                .collect(Collectors.toList());

        toPay.forEach(b -> {
            b.setPaid(true);
            bookingRepository.save(b);
        });

        // Notify buyer: payment went through
        notificationService.addBuyerNotification(userId,
            "Payment of $" + String.format("%.2f", amount) + " received. Your booking is now fully paid.");

        // Notify owner for each paid booking
        toPay.forEach(b -> {
            var car = b.getCarId() != null ? carRepository.findById(b.getCarId()).orElse(null) : null;
            String carName = car != null ? car.getMake() + " " + car.getModel() : "Car #" + b.getCarId();
            notificationService.addOwnerNotification(car != null ? car.getOwnerId() : -1L,
                "Payment received for your listing: " + carName
                + " (" + b.getStartDate() + " to " + b.getEndDate()
                + "). Amount: $" + String.format("%.2f", b.getTotalCost()) + ".");
        });

        return ResponseEntity.ok("Payment of $" + String.format("%.2f", amount) + " processed successfully!");
    }

    @GetMapping("/invoice")
    public ResponseEntity<Map<String, Object>> getInvoice(
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        Long userId = resolveUserId(token);

        // Only CONFIRMED + unpaid bookings belonging to this user
        List<Booking> due = bookingRepository.findAll().stream()
                .filter(b -> userId.equals(b.getUserId())
                          && "CONFIRMED".equals(b.getStatus())
                          && !b.isPaid())
                .collect(Collectors.toList());

        double subtotal   = due.stream().mapToDouble(Booking::getTotalCost).sum();
        double serviceFee = Math.round(subtotal * 0.05 * 100.0) / 100.0;
        double total      = Math.round((subtotal + serviceFee) * 100.0) / 100.0;

        return ResponseEntity.ok(Map.of(
            "subtotal",   Math.round(subtotal * 100.0) / 100.0,
            "serviceFee", serviceFee,
            "total",      total
        ));
    }
}
