package com.solutis.ticketservice.service;

import com.solutis.ticketservice.config.UserServiceClient;
import com.solutis.ticketservice.dto.AssignTechnicianRequest;
import com.solutis.ticketservice.dto.CreateTicketRequest;
import com.solutis.ticketservice.dto.TicketResponse;
import com.solutis.ticketservice.dto.UpdateTicketRequest;
import com.solutis.ticketservice.entity.Category;
import com.solutis.ticketservice.entity.Priority;
import com.solutis.ticketservice.entity.Status;
import com.solutis.ticketservice.entity.Ticket;
import com.solutis.ticketservice.event.TicketEventPublisher;
import com.solutis.ticketservice.exception.TicketNotFoundException;
import com.solutis.ticketservice.exception.UserServiceException;
import com.solutis.ticketservice.repository.TicketRepository;
import com.solutis.ticketservice.repository.TicketSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserServiceClient userServiceClient;
    private final TicketEventPublisher ticketEventPublisher;

    @Transactional
    public TicketResponse create(
            CreateTicketRequest request,
            Authentication authentication
    ) {

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        UUID authenticatedUserId =
                UUID.fromString(authentication.getName());

        UUID customerId = request.customerId();

        if ("ROLE_CLIENT".equals(role)) {
            customerId = authenticatedUserId;
        }

        UserServiceClient.UserResponse customer =
                userServiceClient.findUser(customerId);

        if (!customer.active()) {
            throw new UserServiceException(
                    "Cliente está inativo: " + customerId
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
                .customerId(customerId)
                .status(Status.OPEN)
                .build();

        Ticket savedTicket =
                ticketRepository.save(ticket);

        ticketEventPublisher.publishCreated(
                savedTicket
        );

        return TicketResponse.fromEntity(
                savedTicket
        );
    }

    @Transactional(readOnly = true)
    public TicketResponse findById(
            UUID id,
            Authentication authentication
    ) {

        Ticket ticket = findTicket(id);

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        if ("ROLE_CLIENT".equals(role)) {

            UUID authenticatedUserId =
                    UUID.fromString(
                            authentication.getName()
                    );

            if (!ticket.getCustomerId()
                    .equals(authenticatedUserId)) {

                throw new AccessDeniedException(
                        "Você não possui permissão para acessar este chamado"
                );
            }
        }

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> findAll(
            String search,
            Status status,
            Priority priority,
            Category category,
            UUID customerId,
            Authentication authentication
    ) {

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        if ("ROLE_CLIENT".equals(role)) {
            customerId =
                    UUID.fromString(
                            authentication.getName()
                    );
        }

        Specification<Ticket> specification =
                Specification.allOf(
                        TicketSpecifications.isActive(),
                        TicketSpecifications
                                .titleOrDescriptionContains(
                                        search
                                ),
                        TicketSpecifications.hasStatus(
                                status
                        ),
                        TicketSpecifications.hasPriority(
                                priority
                        ),
                        TicketSpecifications.hasCategory(
                                category
                        ),
                        TicketSpecifications
                                .belongsToCustomer(
                                        customerId
                                )
                );

        return ticketRepository
                .findAll(specification)
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

        Status previousStatus =
                ticket.getStatus();

        ticket.setDescription(
                request.description()
        );

        ticket.setCategory(
                request.category()
        );

        ticket.setPriority(
                request.priority()
        );

        ticket.setStatus(
                request.status()
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        if (previousStatus !=
                savedTicket.getStatus()) {

            ticketEventPublisher
                    .publishStatusChanged(
                            savedTicket,
                            previousStatus.name(),
                            savedTicket
                                    .getStatus()
                                    .name()
                    );
        }

        return TicketResponse.fromEntity(
                savedTicket
        );
    }

    @Transactional
    public TicketResponse assignTechnician(
            UUID id,
            AssignTechnicianRequest request
    ) {

        Ticket ticket = findTicket(id);

        UUID currentTechnicianId =
                ticket.getTechnicianId();

        if (request.technicianId()
                .equals(currentTechnicianId)) {

            throw new UserServiceException(
                    "Este técnico já está atribuído a este chamado"
            );
        }

        UserServiceClient.UserResponse technician =
                userServiceClient.findUser(
                        request.technicianId()
                );

        if (!technician.active()) {
            throw new UserServiceException(
                    "Técnico está inativo: " +
                            request.technicianId()
            );
        }

        if (!"TECHNICIAN".equals(
                technician.role()
        )) {
            throw new UserServiceException(
                    "Usuário informado não possui o perfil TECHNICIAN"
            );
        }

        ticket.setTechnicianId(
                request.technicianId()
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        ticketEventPublisher.publishAssigned(
                savedTicket
        );

        return TicketResponse.fromEntity(
                savedTicket
        );
    }

    @Transactional
    public TicketResponse claim(
            UUID id,
            Authentication authentication
    ) {

        Ticket ticket = findTicket(id);

        if (ticket.getTechnicianId() != null) {
            throw new UserServiceException(
                    "Este chamado já possui um técnico atribuído"
            );
        }

        UUID technicianId =
                UUID.fromString(
                        authentication.getName()
                );

        UserServiceClient.UserResponse technician =
                userServiceClient.findUser(
                        technicianId
                );

        if (!technician.active()) {
            throw new UserServiceException(
                    "Seu usuário está inativo"
            );
        }

        if (!"TECHNICIAN".equals(
                technician.role()
        )) {
            throw new UserServiceException(
                    "Apenas usuários TECHNICIAN podem assumir chamados"
            );
        }

        ticket.setTechnicianId(
                technicianId
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        ticketEventPublisher.publishAssigned(
                savedTicket
        );

        return TicketResponse.fromEntity(
                savedTicket
        );
    }

    @Transactional
    public void close(UUID id) {

        Ticket ticket = findTicket(id);

        Status previousStatus =
                ticket.getStatus();

        ticket.setStatus(Status.CLOSED);

        Ticket savedTicket =
                ticketRepository.save(ticket);

        if (previousStatus != Status.CLOSED) {

            ticketEventPublisher
                    .publishStatusChanged(
                            savedTicket,
                            previousStatus.name(),
                            Status.CLOSED.name()
                    );
        }
    }

    @Transactional
    public void delete(UUID id) {

        Ticket ticket = findTicket(id);

        ticket.setActive(false);

        ticketRepository.save(ticket);
    }

    private Ticket findTicket(UUID id) {

        return ticketRepository
                .findById(id)
                .filter(Ticket::isActive)
                .orElseThrow(() ->
                        new TicketNotFoundException(
                                "Chamado não encontrado: " +
                                        id
                        )
                );
    }
}