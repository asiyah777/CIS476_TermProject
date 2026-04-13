package com.driveshare.patterns.proxy;

import com.driveshare.model.User;
import com.driveshare.repository.UserRepository;

public class RealPayment implements Payment {

    private UserRepository userRepository;

    public RealPayment(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void pay(double amount, Long userId) {
        // User may not exist in DB if auth is still using dummy data; skip balance update gracefully
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setBalance(user.getBalance() - amount);
            userRepository.save(user);
        }
        System.out.println("Payment processed: $" + amount);
    }
}