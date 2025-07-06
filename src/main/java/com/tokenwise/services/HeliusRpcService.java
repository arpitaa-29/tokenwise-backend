package com.tokenwise.services;

import com.tokenwise.DTOs.getTokenAccountsResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Service
public class HeliusRpcService {

    private final String API_KEY = "49d3a7a0-35ca-4006-8d3a-b297f1ad1bde";  // replace with your actual key
    private final String HELIUS_URL = "https://mainnet.helius-rpc.com/?api-key=" + API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${tokenwise.page.limit:1000}") // default 1000 if not set
    private int defaultLimit;

    public getTokenAccountsResponseDTO getTokenAccounts(String mint, String cursor, Integer limit) {
        int useLimit = (limit == null) ? defaultLimit : limit;
        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("jsonrpc", "2.0");
        requestBody.put("id", 1);
        requestBody.put("method", "getTokenAccounts");

        Map<String, Object> params = new HashMap<>();
        params.put("mint", mint);
        params.put("limit", useLimit);
        params.put("cursor", cursor);  // can be null for first call

        requestBody.put("params", params);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Make POST request
        ResponseEntity<getTokenAccountsResponseDTO> response = restTemplate.exchange(
                HELIUS_URL,
                HttpMethod.POST,
                requestEntity,
                getTokenAccountsResponseDTO.class);

        return response.getBody();
    }
}

