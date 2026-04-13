package com.driveshare.patterns.observer;

import com.driveshare.model.Car;
import java.util.ArrayList;
import java.util.List;

public class CarSubject implements Subject {

    private Car car;
    private List<Observer> observers = new ArrayList<>();

    public CarSubject(Car car) {
        this.car = car;
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer o : observers) {
            o.update(car, message);
        }
    }
}