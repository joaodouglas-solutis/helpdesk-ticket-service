package com.solutis.ticketservice.dto;

import com.solutis.ticketservice.entity.Category;
import com.solutis.ticketservice.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTicketRequest(

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 150, message = "Título deve possuir no máximo 150 caracteres")
        String title,

        @NotBlank(message = "Descrição é obrigatória")
        String description,

        @NotNull(message = "Categoria é obrigatória")
        Category category,

        @NotNull(message = "Prioridade é obrigatória")
        Priority priority,

        @NotNull(message = "Cliente é obrigatório")
        UUID customerId
) {
}
