package com.solutis.ticketservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketCreatedEvent(
        UUID ticketId,
        String title,
        UUID customerId,
        LocalDateTime createdAt
) {
}
