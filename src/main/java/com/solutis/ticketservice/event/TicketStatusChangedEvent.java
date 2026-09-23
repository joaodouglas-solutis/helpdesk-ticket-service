package com.solutis.ticketservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketStatusChangedEvent(
        UUID ticketId,
        UUID customerId,
        String previousStatus,
        String newStatus,
        LocalDateTime changedAt
) {
}