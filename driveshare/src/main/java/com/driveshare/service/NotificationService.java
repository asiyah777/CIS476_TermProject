package com.driveshare.service;

import com.driveshare.model.Car;
import com.driveshare.model.User;
import com.driveshare.patterns.observer.CarSubject;
import com.driveshare.patterns.observer.UserObserver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private Map<Long, CarSubject> subjects = new HashMap<>();

    // User-specific notification storage: userId -> notifications
    private Map<Long, List<String>> userNotifications = new HashMap<>();

    public void watchCar(User user, Car car) {
        CarSubject subject = subjects.computeIfAbsent(car.getId(), k -> new CarSubject(car));
        subject.registerObserver(new UserObserver(user));
    }

    /** Notify watchers of a car about updates (price drops, etc.) */
    public void notifyCarUpdate(Car car, String message) {
        CarSubject subject = subjects.get(car.getId());
        if (subject != null) {
            subject.notifyObservers(message);
        }
    }

    /** Add a notification for a specific user (owner). */
    public void addOwnerNotification(Long userId, String message) {
        userNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    }

    /** Add a notification for a specific user (buyer/renter). */
    public void addBuyerNotification(Long userId, String message) {
        userNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    }

    /** Get all notifications for a specific user. */
    public List<String> getUserNotifications(Long userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    // Legacy methods for backwards compatibility (deprecated)
    @Deprecated
    public void addOwnerNotification(String message) {
        // This is no longer used - kept for compatibility
    }

    @Deprecated
    public void addBuyerNotification(String message) {
        // This is no longer used - kept for compatibility
    }

    @Deprecated
    public List<String> getOwnerInbox() {
        return new ArrayList<>();
    }

    @Deprecated
    public List<String> getBuyerInbox() {
        return new ArrayList<>();
    }
}