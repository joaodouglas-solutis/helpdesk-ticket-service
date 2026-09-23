package com.solutis.ticketservice.repository;

import com.solutis.ticketservice.entity.Category;
import com.solutis.ticketservice.entity.Priority;
import com.solutis.ticketservice.entity.Status;
import com.solutis.ticketservice.entity.Ticket;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }

    public static Specification<Ticket> hasStatus(Status status) {
        return (root, query, cb) ->
                status == null
                        ? null
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasPriority(Priority priority) {
        return (root, query, cb) ->
                priority == null
                        ? null
                        : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> hasCategory(Category category) {
        return (root, query, cb) ->
                category == null
                        ? null
                        : cb.equal(root.get("category"), category);
    }

    public static Specification<Ticket> belongsToCustomer(UUID customerId) {
        return (root, query, cb) ->
                customerId == null
                        ? null
                        : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<Ticket> titleOrDescriptionContains(String search) {
        return (root, query, cb) -> {

            if (search == null || search.isBlank()) {
                return null;
            }

            String value = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(
                            cb.lower(root.get("title")),
                            value
                    ),
                    cb.like(
                            cb.lower(root.get("description")),
                            value
                    )
            );
        };
    }
}