package com.solutis.ticketservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTechnicianRequest(

        @NotNull(message = "Técnico é obrigatório")
        UUID technicianId
) {
}