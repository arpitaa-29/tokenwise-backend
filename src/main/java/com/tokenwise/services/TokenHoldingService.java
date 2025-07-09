package com.tokenwise.services;

import com.tokenwise.DTOs.TopHoldersResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenHoldingService {

    private static final Logger log = LoggerFactory.getLogger(TokenHoldingService.class);

    private final HeliusRpcService heliusRpcService;
    private final RedisTemplate<String, List<TopHoldersResponseDTO>> redisTemplate;
    private final WebhookService webhookService;

    // Fetch and compute top holders from RPC, with BigDecimal for precision
    public List<TopHoldersResponseDTO> getTopTokenHoldersFromRpc(String mint, int topN, int decimals) {
        Map<String, BigDecimal> ownerToAmount = new HashMap<>();
        String cursor = null;

        while (true) {
            var response = heliusRpcService.getTokenAccounts(mint, cursor, 1000);
            if (response == null || response.getResult() == null) break;

            var accounts = response.getResult().getToken_accounts();
            if (accounts == null || accounts.isEmpty()) break;

            for (var account : accounts) {
                ownerToAmount.merge(
                        account.getOwner(),
                        BigDecimal.valueOf(account.getAmount()), // Correct way
                        BigDecimal::add
                );
            }

            cursor = response.getResult().getCursor();
            if (cursor == null) break;
        }

        DecimalFormat df = new DecimalFormat("0.######");
        return ownerToAmount.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> new TopHoldersResponseDTO(
                        entry.getKey(),
                        entry.getValue().longValue(),
                        df.format(entry.getValue().movePointLeft(decimals))
                ))
                .collect(Collectors.toList());
    }

    // Scheduled cache update with expiration (5 minutes)
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void updateTopHoldersCache() {
        String mint = "9BB6NFEcjBCtnNLFko2FqVQBq8HHM13kCyYcdQbgpump";
        int topN = 150;
        int decimals = 6;
        String cacheKey = "topTokenHolders::" + mint;

        List<TopHoldersResponseDTO> latest = getTopTokenHoldersFromRpc(mint, topN, decimals);
        redisTemplate.opsForValue().set(cacheKey, latest, 6, TimeUnit.MINUTES); // 6 min expiry for safety
        log.info("[✓] Redis cache refreshed at {} for mint {}", new Date(), mint);

        List<String> walletAddresses = latest.stream()
                .map(TopHoldersResponseDTO::getOwner)
                .collect(Collectors.toList());

        if (!walletAddresses.isEmpty()) {
            webhookService.registerWebhook(walletAddresses);
        } else {
            log.warn("[!] No wallet addresses found for webhook registration.");
        }
    }

    // Get top holders from cache or fetch and cache if missing
    public List<TopHoldersResponseDTO> getTopTokenHolders(String mint, int topN, int decimals) {
        String cacheKey = "topTokenHolders::" + mint;
        ValueOperations<String, List<TopHoldersResponseDTO>> ops = redisTemplate.opsForValue();

        List<TopHoldersResponseDTO> cached = ops.get(cacheKey);

        if (cached != null) {
            log.info("[CACHE HIT] Returning top holders from Redis for mint {}", mint);
            return cached;
        }

        log.info("[CACHE MISS] Fetching from Helius RPC for mint {}", mint);
        List<TopHoldersResponseDTO> fresh = getTopTokenHoldersFromRpc(mint, topN, decimals);
        ops.set(cacheKey, fresh, 6, TimeUnit.MINUTES); // cache result for 6 minutes
        return fresh;
    }
}
