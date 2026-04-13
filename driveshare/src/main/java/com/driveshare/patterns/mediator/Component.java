package com.driveshare.patterns.mediator;

public abstract class Component {

    protected Mediator mediator;

    public Component(Mediator mediator) {
        this.mediator = mediator;
    }
}
