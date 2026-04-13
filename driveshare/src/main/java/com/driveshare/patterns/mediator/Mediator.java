package com.driveshare.patterns.mediator;

import com.driveshare.model.Booking;
import com.driveshare.model.Car;
import com.driveshare.service.NotificationService;

public interface Mediator {
    void notify(Component sender, String event);

    void processBookingRequest(Booking booking, Car car, String dates);
    void processBookingConfirmation(Booking booking, Car car);
    void processBookingDenial(Booking booking, Car car);
}
