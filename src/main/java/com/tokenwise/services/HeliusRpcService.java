package com.tokenwise.services;

import com.tokenwise.DTOs.GetTokenAccountsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HeliusRpcService {

    @Value("${helius.api.key}")
    private String apiKey;

    @Value("${helius.url:https://mainnet.helius-rpc.com}")
    private String heliusBaseUrl;

    @Value("${tokenwise.page.limit:1000}")
    private int defaultLimit;

    private final RestTemplate restTemplate;

    public GetTokenAccountsResponseDTO getTokenAccounts(String mint, String cursor, Integer limit) {
        int useLimit = (limit == null) ? defaultLimit : limit;

        String url = heliusBaseUrl + "/?api-key=" + apiKey;

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("jsonrpc", "2.0");
        requestBody.put("id", 1);
        requestBody.put("method", "getTokenAccounts");

        Map<String, Object> params = new HashMap<>();
        params.put("mint", mint);
        params.put("limit", useLimit);
        if (cursor != null) params.put("cursor", cursor);

        requestBody.put("params", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GetTokenAccountsResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    GetTokenAccountsResponseDTO.class
            );
            return response.getBody();
        } catch (RestClientException ex) {
            // Log error, optionally throw custom exception
            // log.error("Helius API error", ex);
            return null;
        }
    }
}
