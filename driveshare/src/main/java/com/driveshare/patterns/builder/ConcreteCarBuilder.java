package com.driveshare.patterns.builder;

import com.driveshare.model.Car;

public class ConcreteCarBuilder extends CarBuilder {

    private Car car;

    public ConcreteCarBuilder() {
        car = new Car();
    }

    @Override
    public CarBuilder setMake(String make) {
        car.setMake(make);
        return this;
    }

    @Override
    public CarBuilder setModel(String model) {
        car.setModel(model);
        return this;
    }

    @Override
    public CarBuilder setCarYear(int carYear) {
        car.setCarYear(carYear);
        return this;
    }

    @Override
    public CarBuilder setMileage(double mileage) {
        car.setMileage(mileage);
        return this;
    }

    @Override
    public CarBuilder setLocation(String location) {
        car.setLocation(location);
        return this;
    }

    @Override
    public CarBuilder setPrice(double price) {
        car.setPricePerDay(price);
        return this;
    }

    @Override
    public Car build() {
        return car;
    }
}
