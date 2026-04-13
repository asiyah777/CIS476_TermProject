package com.driveshare.patterns.mediator;

import com.driveshare.model.Booking;
import com.driveshare.model.Car;
import com.driveshare.service.NotificationService;

public class UIMediator implements Mediator {

    private NotificationService notificationService;

    public UIMediator(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void notify(Component sender, String event) {
        if (sender instanceof BookingComponent) {
            System.out.println("Booking event: " + event);
        }
    }

    @Override
    public void processBookingRequest(Booking booking, Car car, String dates) {
        String carName = car.getMake() + " " + car.getModel();

        notify(new BookingComponent(this), "Request submitted for " + carName + " (" + dates + ")");

        // Owner notification - someone wants to book their car
        notificationService.addOwnerNotification(car.getOwnerId(),
            "New booking request for " + carName + " (" + dates + ") — awaiting your confirmation.");

        // Buyer notification - confirmation that request was submitted
        notificationService.addBuyerNotification(booking.getUserId(),
            "Your booking request for " + carName + " (" + dates + ") was submitted. Awaiting owner confirmation.");
    }

    @Override
    public void processBookingConfirmation(Booking booking, Car car) {
        String carName = car != null ? car.getMake() + " " + car.getModel() : "Car #" + booking.getCarId();
        String confirmMsg = "Booking confirmed for " + carName + " (" + booking.getStartDate()
            + " to " + booking.getEndDate() + "). Total: $"
            + String.format("%.2f", booking.getTotalCost()) + ". Payment is now due.";

        // Coordinate: Log the event + Send real notification
        notify(new BookingComponent(this), "Confirmed booking #" + booking.getId() + " for " + carName);

        // Buyer notification - their request was confirmed and payment is due
        notificationService.addBuyerNotification(booking.getUserId(), confirmMsg);
    }

    @Override
    public void processBookingDenial(Booking booking, Car car) {
        String carName = car != null ? car.getMake() + " " + car.getModel() : "Car #" + booking.getCarId();

        // Coordinate: Log the event + Send real notification
        notify(new BookingComponent(this), "Denied booking #" + booking.getId() + " for " + carName);

        // Buyer notification - their request was denied
        notificationService.addBuyerNotification(booking.getUserId(),
            "Your booking request for " + carName + " (" + booking.getStartDate()
            + " to " + booking.getEndDate() + ") was denied by the owner.");
    }
}
