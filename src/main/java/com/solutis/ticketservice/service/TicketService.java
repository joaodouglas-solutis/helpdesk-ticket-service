package com.solutis.ticketservice.service;

import com.solutis.ticketservice.dto.AssignTechnicianRequest;
import com.solutis.ticketservice.dto.CreateTicketRequest;
import com.solutis.ticketservice.dto.TicketResponse;
import com.solutis.ticketservice.dto.UpdateTicketRequest;
import com.solutis.ticketservice.entity.Status;
import com.solutis.ticketservice.entity.Ticket;
import com.solutis.ticketservice.event.TicketEventPublisher;
import com.solutis.ticketservice.exception.TicketNotFoundException;
import com.solutis.ticketservice.exception.UserServiceException;
import com.solutis.ticketservice.repository.TicketRepository;
import com.solutis.ticketservice.repository.TicketSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.solutis.ticketservice.config.UserServiceClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserServiceClient userServiceClient;
    private final TicketEventPublisher ticketEventPublisher;

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {

        UserServiceClient.UserResponse customer =
                userServiceClient.findUser(request.customerId());

        if (!customer.active()) {
            throw new UserServiceException(
                    "Cliente está inativo: " + request.customerId()
            );
        }

        if (!"CLIENT".equals(customer.role())) {
            throw new UserServiceException(
                    "Usuário informado não possui o perfil CLIENT"
            );
        }

        Ticket ticket = Ticket.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .priority(request.priority())
                .customerId(request.customerId())
                .status(Status.OPEN)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        ticketEventPublisher.publishCreated(savedTicket);

        return TicketResponse.fromEntity(savedTicket);
    }
    @Transactional(readOnly = true)
    public TicketResponse findById(UUID id) {

        Ticket ticket = findTicket(id);

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> findAll(
            String search,
            Status status,
            com.solutis.ticketservice.entity.Priority priority,
            com.solutis.ticketservice.entity.Category category,
            UUID customerId
    ) {

        Specification<Ticket> specification = Specification.allOf(
                TicketSpecifications.titleOrDescriptionContains(search),
                TicketSpecifications.hasStatus(status),
                TicketSpecifications.hasPriority(priority),
                TicketSpecifications.hasCategory(category),
                TicketSpecifications.belongsToCustomer(customerId)
        );

        return ticketRepository.findAll(specification)
                .stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    @Transactional
    public TicketResponse update(
            UUID id,
            UpdateTicketRequest request
    ) {

        Ticket ticket = findTicket(id);

        ticket.setDescription(request.description());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());
        ticket.setStatus(request.status());

        return TicketResponse.fromEntity(
                ticketRepository.save(ticket)
        );
    }

    @Transactional
    public TicketResponse assignTechnician(
            UUID id,
            AssignTechnicianRequest request
    ) {

        Ticket ticket = findTicket(id);

        UserServiceClient.UserResponse technician =
                userServiceClient.findUser(request.technicianId());

        if (!technician.active()) {
            throw new UserServiceException(
                    "Técnico está inativo: " + request.technicianId()
            );
        }

        if (!"TECHNICIAN".equals(technician.role())) {
            throw new UserServiceException(
                    "Usuário informado não possui o perfil TECHNICIAN"
            );
        }

        ticket.setTechnicianId(request.technicianId());

        return TicketResponse.fromEntity(
                ticketRepository.save(ticket)
        );
    }

    @Transactional
    public void close(UUID id) {

        Ticket ticket = findTicket(id);

        ticket.setStatus(Status.CLOSED);

        ticketRepository.save(ticket);
    }

    @Transactional
    public void delete(UUID id) {

        Ticket ticket = findTicket(id);

        ticketRepository.delete(ticket);
    }

    private Ticket findTicket(UUID id) {

        return ticketRepository.findById(id)
                .orElseThrow(() ->
                        new TicketNotFoundException(
                                "Chamado não encontrado: " + id
                        )
                );
    }
}
