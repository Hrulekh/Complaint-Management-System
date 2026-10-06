package com.cms.complaints.observer;

import org.springframework.stereotype.Component;

@Component
public class OverdueEscalationObserver implements ComplaintObserver {
    @Override
    public void update(ComplaintEvent event) {
        if (event.getType() == ComplaintEvent.EventType.ESCALATED) {
        }
    }
}
