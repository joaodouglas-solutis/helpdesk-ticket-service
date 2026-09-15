package com.solutis.ticketservice.event;

import com.solutis.ticketservice.config.RabbitMQConfig;
import com.solutis.ticketservice.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

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
}