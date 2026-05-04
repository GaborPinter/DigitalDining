package com.DigitalDining.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;

@Service
public class PaypalService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url:https://api-m.sandbox.paypal.com}")
    private String baseUrl;

    @Value("${paypal.currency:HUF}")
    private String currency;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PaypalService(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        this.restTemplate = builder.build();
        this.objectMapper = objectMapper;
    }

    public String createOrder(BigDecimal amount) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "intent", "CAPTURE",
                    "purchase_units", List.of(
                            Map.of(
                                    "amount", Map.of(
                                            "currency_code", currency,
                                            "value", formatAmountForPaypal(amount)
                                    )
                            )
                    )
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v2/checkout/orders",
                    new HttpEntity<>(body, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("PayPal order létrehozás sikertelen");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String id = root.path("id").asText(null);

            if (id == null || id.isBlank()) {
                throw new IllegalStateException("PayPal order ID nem érkezett vissza");
            }

            return id;
        } catch (Exception e) {
            throw new IllegalStateException("PayPal order létrehozás hiba", e);
        }
    }

    public PaypalCaptureResult captureOrder(String orderId) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v2/checkout/orders/" + orderId + "/capture",
                    new HttpEntity<>("{}", headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("PayPal capture sikertelen");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText(null);

            return new PaypalCaptureResult(orderId, status, response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException("PayPal capture hiba", e);
        }
    }

    private String getAccessToken() {
        try {
            String auth = Base64.getEncoder().encodeToString(
                    (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + auth);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/oauth2/token",
                    new HttpEntity<>(form, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("PayPal access token kérése sikertelen");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String token = root.path("access_token").asText(null);

            if (token == null || token.isBlank()) {
                throw new IllegalStateException("PayPal access token nem érkezett vissza");
            }

            return token;
        } catch (Exception e) {
            throw new IllegalStateException("PayPal access token hiba", e);
        }
    }

    private String formatAmountForPaypal(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }

        if ("HUF".equalsIgnoreCase(currency)
                || "JPY".equalsIgnoreCase(currency)
                || "TWD".equalsIgnoreCase(currency)) {
            return amount.setScale(0, RoundingMode.UNNECESSARY).toPlainString();
        }

        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public record PaypalCaptureResult(String orderId, String status, String rawResponse) {}
}
