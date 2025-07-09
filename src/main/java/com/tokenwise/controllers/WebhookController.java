package com.tokenwise.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.tokenwise.services.TransactionProcessingService;

@RestController
public class WebhookController {

    private final TransactionProcessingService transactionProcessingService;

    public WebhookController(TransactionProcessingService transactionProcessingService) {
        this.transactionProcessingService = transactionProcessingService;
    }

    @PostMapping("/solana-webhook-handler")
    public ResponseEntity<?> handleHeliusWebhook(@RequestBody List<Map<String, Object>> payload) {
        try {
            // Log the raw payload for debugging
            System.out.println("[Webhook] Received event array of size: " + payload.size());
            for (int i = 0; i < payload.size(); i++) {
                System.out.println("[Webhook] Event #" + (i + 1) + ": " + payload.get(i));
            }

            // Process each event in the array
            for (Map<String, Object> event : payload) {
                try {
                    System.out.println("[Webhook] Processing event with signature: " + event.get("signature"));
                    transactionProcessingService.processHeliusWebhookPayload(event);
                } catch (Exception eventEx) {
                    System.err.println("[Webhook] Error processing individual event: " + eventEx.getMessage());
                    eventEx.printStackTrace();
                }
            }

            // Respond quickly to Helius to acknowledge receipt
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("[Webhook] Error processing webhook payload: " + e.getMessage());
            e.printStackTrace();
            // Still respond with 200 OK to avoid Helius retries, unless you want retries
            return ResponseEntity.ok().build();
        }
    }
}

