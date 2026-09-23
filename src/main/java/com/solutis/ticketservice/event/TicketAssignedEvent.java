package com.solutis.ticketservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketAssignedEvent(
        UUID ticketId,
        UUID customerId,
        UUID technicianId,
        LocalDateTime assignedAt
) {
}