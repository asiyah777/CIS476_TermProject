package com.driveshare.patterns.builder;

import com.driveshare.model.Car;

public abstract class CarBuilder {

    public abstract CarBuilder setMake(String make);
    public abstract CarBuilder setModel(String model);
    public abstract CarBuilder setCarYear(int carYear);
    public abstract CarBuilder setMileage(double mileage);
    public abstract CarBuilder setLocation(String location);
    public abstract CarBuilder setPrice(double price);
    public abstract Car build();
}
