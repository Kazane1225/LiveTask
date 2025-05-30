package com.example.livetask.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatGPTService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    public String generatePraise(String taskName) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", "The task '" + taskName + "' has been completed. Based on the name, infer the user's effort and provide a brief compliment in same language that task's name."
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-4o",
                "messages", List.of(message),
                "max_tokens", 70
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(ENDPOINT, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");

        return (String) messageObj.get("content");
    }
}

