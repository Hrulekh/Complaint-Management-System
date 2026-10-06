package com.cms.complaints.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogObserver implements ComplaintObserver {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogObserver.class);

    @Override
    public void update(ComplaintEvent event) {
        logger.info("AUDIT: [{}] Ticket: {} Actor: {} Details: {}",
                event.getType(),
                event.getComplaint().getTicketId(),
                event.getActorName(),
                event.getDetails() != null ? event.getDetails() : "");
    }
}
