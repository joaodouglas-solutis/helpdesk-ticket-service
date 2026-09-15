package com.solutis.ticketservice.dto;

import com.solutis.ticketservice.entity.Category;
import com.solutis.ticketservice.entity.Priority;
import com.solutis.ticketservice.entity.Status;
import com.solutis.ticketservice.entity.Ticket;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(

        UUID id,
        String title,
        String description,
        Priority priority,
        Status status,
        Category category,
        UUID customerId,
        UUID technicianId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static TicketResponse fromEntity(Ticket ticket) {

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCategory(),
                ticket.getCustomerId(),
                ticket.getTechnicianId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
