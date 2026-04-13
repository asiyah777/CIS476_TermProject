package com.driveshare.patterns.proxy;

public interface Payment {
    void pay(double amount, Long userId);
}
