package com.driveshare.service;

import com.driveshare.patterns.proxy.Payment;
import com.driveshare.patterns.proxy.PaymentProxy;
import com.driveshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    public void makePayment(double amount, Long userId) {

        Payment payment = new PaymentProxy(userRepository);

        payment.pay(amount, userId);
    }
}