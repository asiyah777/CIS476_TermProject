package com.driveshare.patterns.chain;

import com.driveshare.model.User;
import java.util.List;

public abstract class Handler {

    protected Handler next;

    public void setNext(Handler next) {
        this.next = next;
    }

    public abstract boolean handle(User user, List<String> answers);
}