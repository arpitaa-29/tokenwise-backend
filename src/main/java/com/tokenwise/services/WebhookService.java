package com.tokenwise.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;


@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    @Value("${helius.api.key}")
    private String heliusApiKey;

    @Value("${helius.webhook.base-url}")
    private String heliusWebhookBaseUrl;

    @Value("${webhook.handler.url}")
    private String webhookHandlerUrl;

    private final RestTemplate restTemplate;

    private String getListUrl() {
        return heliusWebhookBaseUrl + "?api-key=" + heliusApiKey;
    }

    private String getCreateUrl() {
        return heliusWebhookBaseUrl + "?api-key=" + heliusApiKey;
    }

    private String getDeleteUrl(String webhookId) {
        return heliusWebhookBaseUrl + "/" + webhookId + "?api-key=" + heliusApiKey;
    }

    /**
     * Deletes all existing webhooks for this API key that match our handler URL.
     */
    private void deleteAllWebhooks() {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(getListUrl(), List.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> webhooks = response.getBody();
                for (Map<String, Object> webhook : webhooks) {
                    String url = (String) webhook.get("webhookURL");
                    String webhookId = (String) webhook.get("webhookID");
                    if (webhookId != null && webhookHandlerUrl.equals(url)) {
                        restTemplate.delete(getDeleteUrl(webhookId));
                        log.info("[✓] Deleted webhook: {}", webhookId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[!] Exception while deleting webhooks: {}", e.getMessage(), e);
        }
    }

    /**
     * Registers a new webhook with Helius for the given wallet addresses.
     */
    public void registerWebhook(List<String> walletAddresses) {
        // Step 1: Delete all existing matching webhooks
        deleteAllWebhooks();

        // Step 2: Register new webhook
        Map<String, Object> payload = new HashMap<>();
        payload.put("webhookURL", webhookHandlerUrl);
        payload.put("transactionTypes", List.of("ANY"));
        payload.put("accountAddresses", walletAddresses);
        payload.put("webhookType", "enhanced");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        log.info("Registering webhook with URL: {}", webhookHandlerUrl);
        log.info("Payload: {}", payload);


        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    getCreateUrl(), request, String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[✓] Helius webhook registered/updated successfully.");
            } else {
                log.warn("[!] Failed to register/update Helius webhook: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("[!] Exception while registering/updating Helius webhook: {}", e.getMessage(), e);
        }
    }
}
