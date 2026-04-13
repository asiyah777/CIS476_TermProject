package com.driveshare.patterns.builder;

import com.driveshare.model.Car;

public class CarDirector {

    private CarBuilder builder;

    public CarDirector(CarBuilder builder) {
        this.builder = builder;
    }

    public Car createBasicCar(String model, double price) {
        return builder
                .setModel(model)
                .setPrice(price)
                .build();
    }

    /** Builds a fully-specified rental car from all listing fields. */
    public Car createCar(String make, String model, int year,
                         double mileage, String location, double price) {
        return builder
                .setMake(make)
                .setModel(model)
                .setCarYear(year)
                .setMileage(mileage)
                .setLocation(location)
                .setPrice(price)
                .build();
    }
}
