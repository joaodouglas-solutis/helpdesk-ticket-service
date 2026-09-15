package com.solutis.ticketservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.solutis.ticketservice.exception.UserServiceException;

import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(
            RestClient.Builder builder,
            @Value("${services.user-service.url}") String userServiceUrl
    ) {
        this.restClient = builder
                .baseUrl(userServiceUrl)
                .build();
    }

    public UserResponse findUser(UUID userId) {

        return restClient
                .get()
                .uri("/users/{id}", userId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (request, response) -> {
                            throw new UserServiceException(
                                    "Usuário não encontrado: " + userId
                            );
                        }
                )
                .body(UserResponse.class);
    }

    public record UserResponse(
            UUID id,
            String name,
            String email,
            String role,
            boolean active,
            String createdAt
    ) {
    }
}