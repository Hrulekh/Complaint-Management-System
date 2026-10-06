package com.cms.complaints.observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComplaintEventPublisher {
    @Autowired(required = false)
    private List<ComplaintObserver> observers;

    public void subscribe(ComplaintObserver observer) {
        if (observers == null) {
            observers = new java.util.ArrayList<>();
        }
        observers.add(observer);
    }

    public void unsubscribe(ComplaintObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    public void publish(ComplaintEvent event) {
        if (observers != null) {
            observers.forEach(observer -> observer.update(event));
        }
    }
}
