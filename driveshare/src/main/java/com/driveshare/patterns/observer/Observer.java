package com.driveshare.patterns.observer;

import com.driveshare.model.Car;

public interface Observer {
    void update(Car car, String message);
}