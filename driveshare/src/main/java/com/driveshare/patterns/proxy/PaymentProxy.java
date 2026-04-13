package com.driveshare.patterns.proxy;

import com.driveshare.repository.UserRepository;

public class PaymentProxy implements Payment {

    private RealPayment realPayment;

    public PaymentProxy(UserRepository userRepository) {
        this.realPayment = new RealPayment(userRepository);
    }

    @Override
    public void pay(double amount, Long userId) {

        if (amount <= 0) {
            throw new RuntimeException("Invalid payment amount");
        }

        System.out.println("🔐 Proxy: Security check passed");

        // Forward to real payment
        realPayment.pay(amount, userId);
    }
}