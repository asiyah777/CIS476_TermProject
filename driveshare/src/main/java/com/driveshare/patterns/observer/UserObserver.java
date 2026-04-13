package com.driveshare.patterns.observer;

import com.driveshare.model.Car;
import com.driveshare.model.User;

public class UserObserver implements Observer {

    private User user;

    public UserObserver(User user) {
        this.user = user;
    }

    @Override
    public void update(Car car, String message) {
        System.out.println("Notification for " + user.getEmail() +
                ": " + message + " (Car: " + car.getModel() + ")");
    }
}