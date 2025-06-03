package com.estsoft.project3.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AllenApiService {

    @Value("${allen.api.url}")
    private String apiUrl;

    @Value("${allen.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAnswer(String content) {
        String questionUrl = UriComponentsBuilder.fromHttpUrl(apiUrl + "/question")
                .queryParam("content", content)
                .queryParam("client_id", apiKey)
                .toUriString();

        String rawResponse = restTemplate.getForObject(questionUrl, String.class);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            return root.path("content").asText();
        } catch (Exception e) {
            return "Failed to parse response.";
        }

    }

    public void resetState() {
        String deleteUrl = apiUrl + "/reset-state";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = "{\"client_id\":\"" + apiKey + "\"}";
        HttpEntity<String> deleteEntity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                deleteEntity,
                Void.class
        );
    }
}
