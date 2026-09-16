package com.solutis.ticketservice.controller;

import com.solutis.ticketservice.dto.AssignTechnicianRequest;
import com.solutis.ticketservice.dto.CreateTicketRequest;
import com.solutis.ticketservice.dto.TicketResponse;
import com.solutis.ticketservice.dto.UpdateTicketRequest;
import com.solutis.ticketservice.entity.Category;
import com.solutis.ticketservice.entity.Priority;
import com.solutis.ticketservice.entity.Status;
import com.solutis.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication
    ) {

        TicketResponse response = ticketService.create(request, authentication);

        return ResponseEntity
                .created(URI.create("/tickets/" + response.id()))
                .body(response);
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'TECHNICIAN', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<TicketResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) UUID customerId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                ticketService.findAll(
                        search,
                        status,
                        priority,
                        category,
                        customerId,
                        authentication
                )
        );
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'TECHNICIAN', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                ticketService.findById(id, authentication)
        );
    }
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {

        return ResponseEntity.ok(
                ticketService.update(id, request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTechnicianRequest request
    ) {

        return ResponseEntity.ok(
                ticketService.assignTechnician(id, request)
        );
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> close(
            @PathVariable UUID id
    ) {

        ticketService.close(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {

        ticketService.delete(id);

        return ResponseEntity.noContent().build();
    }
}