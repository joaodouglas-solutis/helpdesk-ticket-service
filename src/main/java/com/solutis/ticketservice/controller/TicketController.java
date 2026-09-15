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
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @Valid @RequestBody CreateTicketRequest request
    ) {

        TicketResponse response = ticketService.create(request);

        return ResponseEntity
                .created(URI.create("/tickets/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) UUID customerId
    ) {

        return ResponseEntity.ok(
                ticketService.findAll(
                        search,
                        status,
                        priority,
                        category,
                        customerId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                ticketService.findById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {

        return ResponseEntity.ok(
                ticketService.update(id, request)
        );
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTechnicianRequest request
    ) {

        return ResponseEntity.ok(
                ticketService.assignTechnician(id, request)
        );
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> close(
            @PathVariable UUID id
    ) {

        ticketService.close(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {

        ticketService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
