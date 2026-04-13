package com.driveshare.patterns.mediator;

public class BookingComponent extends Component {

    public BookingComponent(Mediator mediator) {
        super(mediator);
    }

    public void book(String bookingInfo) {
        mediator.notify(this, bookingInfo);
    }
}
