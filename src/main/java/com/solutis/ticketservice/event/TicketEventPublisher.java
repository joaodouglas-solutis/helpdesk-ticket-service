package com.solutis.ticketservice.event;

import com.solutis.ticketservice.config.RabbitMQConfig;
import com.solutis.ticketservice.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TicketEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(Ticket ticket) {

        TicketCreatedEvent event = new TicketCreatedEvent(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getCustomerId(),
                ticket.getCreatedAt()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TICKET_EXCHANGE,
                "ticket.created",
                event
        );
    }

    public void publishAssigned(Ticket ticket) {

        TicketAssignedEvent event = new TicketAssignedEvent(
                ticket.getId(),
                ticket.getCustomerId(),
                ticket.getTechnicianId(),
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TICKET_EXCHANGE,
                "ticket.assigned",
                event
        );
    }

    public void publishStatusChanged(
            Ticket ticket,
            String previousStatus,
            String newStatus
    ) {

        TicketStatusChangedEvent event = new TicketStatusChangedEvent(
                ticket.getId(),
                ticket.getCustomerId(),
                previousStatus,
                newStatus,
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TICKET_EXCHANGE,
                "ticket.status-changed",
                event
        );
    }
}