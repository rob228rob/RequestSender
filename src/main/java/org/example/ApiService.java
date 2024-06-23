package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ApiService {

    private static final WebClient webClient = WebClient.create("http://193.19.100.32:7000");
    private static final Logger logger = Logger.getLogger(ApiService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Candidate {
        private String last_name;
        private String first_name;
        private String email;
        private String role;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusResponse {
        private String token;
        private String status;
    }

    public static List<String> getRolesGETRequest() {
        logger.info("Starting getRole() method...");
        var rolesResponse = webClient.get()
                .uri("/api/get-roles")
                .retrieve()
                .bodyToMono(RolesWrapper.class)
                .block();

        List<String> roles = new ArrayList<>();
        var resultRole = rolesResponse.getRoles();
        if (resultRole == null) {
            throw new RuntimeException("getRole() method returned a list of roles");
        }
        logger.info("getRole() method completed. Role received ");
        return rolesResponse.getRoles();
    }

    public static ResponseEntity<String> signUpPOSTRequest(String name, String surname, String email, String role) {
        logger.info("Starting signUp() method with initials: " + name + " " + surname + " email: " + email + " and role: " + role);
        Candidate candidate = new Candidate(surname, name, email, role);
        String candidateJson;
        try {
            candidateJson = objectMapper.writeValueAsString(candidate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ResponseEntity<String> response = webClient.post()
                .uri("/api/sign-up")
                .bodyValue(candidateJson)
                .retrieve()
                .toEntity(String.class)
                .block();
        logger.info("signUp() method completed. User with n&s: " + name + " " + surname + " and role: " + role + " signed up successfully.");

        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        var redirectURL = response.getHeaders().getLocation();
        if (response.getStatusCode().is3xxRedirection() && redirectURL != null) {
            ResponseEntity<String> responeToRedirect = webClient.post()
                    .uri(redirectURL.toString())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(candidateJson)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return responeToRedirect;
        }

        return response;
    }

    public static String getCodeGETRequest(String email) {
        logger.info("Starting getCode() method for email: " + email);
        ResponseEntity<String> responseEntity = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/get-code")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .toEntity(String.class)
                .block();

        if (responseEntity.getStatusCode().is3xxRedirection() && responseEntity.getHeaders().getLocation() != null) {
            ResponseEntity<String> responseEntityRedirection = webClient.get()
                    .uri(responseEntity.getHeaders().getLocation())
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return responseEntityRedirection.getBody();
        }

        logger.info("getCode() method completed. Code received for email " + email + ": " + responseEntity.getBody());
        return responseEntity.getBody();
    }

    public static void setStatusPOSTRequest(String email, String code, String status) {
        logger.info("Starting setStatus() method with email: " + email + " and code: " + code);
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.setToken(TokenDecoder.encodeToken(email, code));
        statusResponse.setStatus("increased");

        String statusJson;
        try {
            statusJson = objectMapper.writeValueAsString(statusResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ResponseEntity<String> responseSetStatus = webClient.post()
                .uri("/api/set-status")
                .bodyValue(statusJson)
                .retrieve()
                .toEntity(String.class)
                .block();

        var resonseBody = responseSetStatus.getBody();
        if (resonseBody != null) {
            return;
        }

        var locationHeader = responseSetStatus.getHeaders().getLocation();
        if (locationHeader != null && !responseSetStatus.getStatusCode().is2xxSuccessful()) {
            ResponseEntity<String> responseSetStatusRedirection = webClient.post()
                    .uri(locationHeader.toString())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(statusResponse)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            logger.info("setStatus() method completed WITH Redirection. Status set successfully with token; Server Response: " + responseSetStatusRedirection.getBody());
            return;
        }

        logger.info("setStatus() method completed. Status set successfully with token; Server Response: " + responseSetStatus.getBody());
    }
}

