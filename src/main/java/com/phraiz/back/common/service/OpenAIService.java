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

    public String callParaphraseOpenAI(String text, String mode, int scale) {
        // 1. scale 값에 따라 temperature, top_p 파라미터 계산
        //    파이썬 코드의 lerp 함수와 동일한 역할
        double temperature = 0.1 + (0.9 - 0.1) * (scale / 100.0);
        double topP = 0.6 + (1.0 - 0.6) * (scale / 100.0);

        // 2. scale 포함하여 프롬프트 구체화
        String systemMessage = "당신은 문장을 다양한 스타일로 바꿔주는 전문가입니다. 사용자가 제공하는 강도(scale)에 맞춰 문장을 다시 작성하세요.";
        String prompt = String.format(
                "모드: %s\n강도: %d/100\n\n다음 문장을 다시 작성해줘: %s", mode, scale, text);

//        String prompt = String.format(
//                "%s 모드로 다음 문장을 바꿔줘: %s", mode, text);

       // return callOpenAIInternal(prompt, "당신은 문장을 다양한 스타일로 바꿔주는 전문가입니다.", gptConfig.getTemperatureParaphrase());
        return callOpenAIInternal(prompt, systemMessage, temperature, topP);
    }

    public String callSummaryOpenAI(String text, String mode) {
        String prompt = String.format("%s: %s",mode, text);
        return callOpenAIInternal(prompt, "당신은 문서를 다양한 방식으로 요약하는 전문가입니다.", gptConfig.getTemperatureSummary(), null);
    }

    private String callOpenAIInternal(String prompt, String systemMessage, Double temperature, Double topP) {
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
        // top_p가 null이 아니면 추가
        if (topP != null) {
            requestBody.put("top_p", topP);
        }
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
