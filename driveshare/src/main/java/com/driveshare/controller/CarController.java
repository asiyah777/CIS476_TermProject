package com.driveshare.controller;

import com.driveshare.dto.CarDTO;
import com.driveshare.model.Car;
import com.driveshare.model.User;
import com.driveshare.model.Watchlist;
import com.driveshare.repository.CarRepository;
import com.driveshare.repository.UserRepository;
import com.driveshare.repository.WatchlistRepository;
import com.driveshare.service.NotificationService;
import com.driveshare.service.UserService;
import com.driveshare.patterns.builder.CarDirector;
import com.driveshare.patterns.builder.ConcreteCarBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cars")
public class CarController extends BaseController {

    @Autowired private UserRepository      userRepository;
    @Autowired private UserService         userService;
    @Autowired private CarRepository       carRepository;
    @Autowired private WatchlistRepository watchlistRepository;
    @Autowired private NotificationService notificationService;

    private CarDTO toDTO(Car car) {
        CarDTO dto = new CarDTO();
        dto.setId(car.getId());
        dto.setOwnerId(car.getOwnerId());
        dto.setMake(car.getMake());
        dto.setModel(car.getModel());
        dto.setCarYear(car.getCarYear());
        dto.setMileage(car.getMileage());
        dto.setLocation(car.getLocation());
        dto.setPricePerDay(car.getPricePerDay());
        dto.setAvailableFrom(car.getAvailableFrom());
        dto.setAvailableTo(car.getAvailableTo());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<CarDTO>> listCars() {
        List<CarDTO> dtos = carRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** Returns only the cars listed by the current session user. */
    @GetMapping("/my")
    public ResponseEntity<List<CarDTO>> getMyCars(
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        Long ownerId = resolveUserId(token);
        List<CarDTO> dtos = carRepository.findAll().stream()
                .filter(c -> ownerId.equals(c.getOwnerId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** Renter watches a car and optionally specifies a target price. */
    @PostMapping("/watch")
    public ResponseEntity<String> watchCar(
            @RequestHeader(value = "X-Session-Token", required = false) String token,
            @RequestBody Map<String, Object> req) {

        Long   carId       = Long.parseLong(req.get("carId").toString());
        double targetPrice = req.containsKey("targetPrice")
                ? Double.parseDouble(req.get("targetPrice").toString()) : 0.0;

        User user = resolveUser(token);
        Car  car  = carRepository.findById(carId).orElse(null);
        if (car == null) return ResponseEntity.badRequest().body("Car not found.");

        Watchlist entry = new Watchlist();
        entry.setUserId(user.getId());
        entry.setCarId(carId);
        entry.setTargetPrice(targetPrice);
        watchlistRepository.save(entry);

        notificationService.watchCar(user, car);

        String msg = targetPrice > 0
            ? "Watching " + car.getMake() + " " + car.getModel()
              + ". You will be notified when the price drops to $"
              + String.format("%.2f", targetPrice) + " or below."
            : "Now watching " + car.getMake() + " " + car.getModel() + ".";
        return ResponseEntity.ok(msg);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarDTO> updateCar(
            @PathVariable Long id,
            @RequestBody CarDTO dto) {

        Car car = carRepository.findById(id).orElse(null);
        if (car == null) return ResponseEntity.notFound().build();

        double oldPrice    = car.getPricePerDay();
        boolean priceDropped = dto.getPricePerDay() > 0 && dto.getPricePerDay() < oldPrice;

        if (dto.getMake() != null)          car.setMake(dto.getMake());
        if (dto.getModel() != null)         car.setModel(dto.getModel());
        if (dto.getCarYear() > 0)           car.setCarYear(dto.getCarYear());
        if (dto.getMileage() >= 0)          car.setMileage(dto.getMileage());
        if (dto.getLocation() != null)      car.setLocation(dto.getLocation());
        if (dto.getPricePerDay() > 0)       car.setPricePerDay(dto.getPricePerDay());
        if (dto.getAvailableFrom() != null) car.setAvailableFrom(dto.getAvailableFrom());
        if (dto.getAvailableTo() != null)   car.setAvailableTo(dto.getAvailableTo());

        Car saved = carRepository.save(car);

        if (priceDropped) {
            double newPrice = saved.getPricePerDay();
            List<Watchlist> watchers = watchlistRepository.findByCarId(id);
            for (Watchlist w : watchers) {
                if (w.getTargetPrice() > 0 && newPrice <= w.getTargetPrice()) {
                    notificationService.addBuyerNotification(w.getUserId(),
                        saved.getMake() + " " + saved.getModel() + " price dropped to $"
                        + String.format("%.2f", newPrice)
                        + " — meets your target of $" + String.format("%.2f", w.getTargetPrice()) + "!");
                } else if (w.getTargetPrice() == 0) {
                    notificationService.addBuyerNotification(w.getUserId(),
                        saved.getMake() + " " + saved.getModel() + " price dropped from $"
                        + String.format("%.2f", oldPrice) + " to $" + String.format("%.2f", newPrice) + ".");
                }
            }
            notificationService.notifyCarUpdate(saved,
                "Price updated to $" + String.format("%.2f", newPrice) + "/day");
        }

        return ResponseEntity.ok(toDTO(saved));
    }

    @GetMapping("/notifications/owner")
    public ResponseEntity<List<String>> getOwnerNotifications(
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        Long userId = resolveUserId(token);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/notifications/buyer")
    public ResponseEntity<List<String>> getBuyerNotifications(
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        Long userId = resolveUserId(token);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PostMapping("/cars")
    public ResponseEntity<Car> addCar(
            @RequestHeader(value = "X-Session-Token", required = false) String token,
            @RequestBody CarDTO dto) {

        Car car = new CarDirector(new ConcreteCarBuilder()).createCar(
                dto.getMake(), dto.getModel(), dto.getCarYear(),
                dto.getMileage(), dto.getLocation(), dto.getPricePerDay());

        car.setOwnerId(resolveUserId(token));
        car.setAvailableFrom(dto.getAvailableFrom());
        car.setAvailableTo(dto.getAvailableTo());

        return ResponseEntity.ok(carRepository.save(car));
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody Map<String, Object> req) {
        try {
            String email = req.get("email").toString();
            Object answersObj = req.get("answers");
            String newPassword = req.get("newPassword").toString();

            // Validate input
            if (email == null || email.trim().isEmpty()) {
                return "Email is required.";
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return "New password is required.";
            }
            if (!(answersObj instanceof List)) {
                return "Security answers must be provided as a list.";
            }

            @SuppressWarnings("unchecked")
            List<String> answers = (List<String>) answersObj;

            if (answers.size() != 3) {
                return "Exactly 3 security answers are required.";
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                return "No account found with that email address.";
            }

            boolean ok = userService.resetPassword(user, answers, newPassword);
            return ok ? "Password reset successful!" : "Security answers incorrect!";
        } catch (Exception e) {
            System.err.println("Error during password reset: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred during password reset. Please try again.";
        }
    }
}
