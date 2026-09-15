package com.solutis.ticketservice.dto;

import com.solutis.ticketservice.entity.Category;
import com.solutis.ticketservice.entity.Priority;
import com.solutis.ticketservice.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketRequest(

        @NotBlank(message = "Descrição é obrigatória")
        String description,

        @NotNull(message = "Categoria é obrigatória")
        Category category,

        @NotNull(message = "Prioridade é obrigatória")
        Priority priority,

        @NotNull(message = "Status é obrigatório")
        Status status
) {
}
