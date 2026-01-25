package com.phraiz.back.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TossPaymentService {

    //Todo. 수정 및 보완

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tosspayments.secret-key}")
    private String tossSecretKey;

    private HttpHeaders createHeaders() {
        String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * 토스 결제 승인 요청
     */
    public ResponseEntity<Map> confirmPayment(String paymentKey, String orderId, Long amount) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", String.valueOf(amount));

        return restTemplate.postForEntity(url, requestBody, Map.class);
    }

    /**
     * 토스 결제 취소 요청(전체 취소)
     */
    public ResponseEntity<Map> canceltotalPayment(String paymentKey, String cancelReason) {
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);

        return restTemplate.postForEntity(url, requestBody, Map.class);
    }

    /**
     * 토스 결제 취소 요청(일부 취소)
     */
    public ResponseEntity<Map> cancelpartialPayment(String paymentKey, String cancelReason, Integer cancelAmount) {
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);
        requestBody.put("cancelAmount", cancelAmount.toString());

        return restTemplate.postForEntity(url, requestBody, Map.class);
    }
}