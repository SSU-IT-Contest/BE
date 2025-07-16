package com.phraiz.back.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phraiz.back.common.config.GptConfig;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service
public class OpenAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GptConfig gptConfig;

    public String callParaphraseOpenAI(String text, String mode) {
        String prompt = String.format(
                "%s 모드로 다음 문장을 바꿔줘: %s", mode, text);
        return callOpenAIInternal(prompt, "당신은 문장을 다양한 스타일로 바꿔주는 전문가입니다.", gptConfig.getTemperatureParaphrase());
    }

    public String callSummaryOpenAI(String text, String mode) {
        String prompt = String.format("%s: %s",mode, text);
        return callOpenAIInternal(prompt, "당신은 문서를 다양한 방식으로 요약하는 전문가입니다.", gptConfig.getTemperatureSummary());
    }

    private String callOpenAIInternal(String prompt, String systemMessage, Double temperature) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptConfig.getSecretKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", gptConfig.getModel());
        requestBody.put("messages", new Object[]{
                new HashMap<String, String>() {{
                    put("role", "system");
                    put("content", systemMessage);
                }},
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
        });
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", gptConfig.getMaxTokens());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    gptConfig.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return extractContentFromResponse(response.getBody());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }



}
