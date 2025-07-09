package com.tokenwise.services;

import com.tokenwise.DTOs.TopHoldersResponseDTO;
import com.tokenwise.models.TransactionEvent;
import com.tokenwise.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import static com.tokenwise.utils.Constants.*;
import static com.tokenwise.utils.Helper.getList;
import static com.tokenwise.utils.Helper.getLong;


@Service
@RequiredArgsConstructor
public class TransactionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessingService.class);


    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String, List<TopHoldersResponseDTO>> redisTemplate;

    // Helper to get top holders set for a mint from Redis
    private Set<String> getTopHoldersSet(String mint) {
        String cacheKey = "topTokenHolders::" + mint;
        List<TopHoldersResponseDTO> topHolders = redisTemplate.opsForValue().get(cacheKey);
        if (topHolders == null) return Collections.emptySet();
        return topHolders.stream()
                .map(TopHoldersResponseDTO::getOwner)
                .collect(Collectors.toSet());
    }

    /**
     * Processes a single event object from the Helius webhook.
     */
    public void processHeliusWebhookPayload(Map<String, Object> payload) {
        try {
            String signature = (String) payload.get("signature");
            Long blockTimeLong = getLong(payload.get("timestamp"));
            Instant blockTime = blockTimeLong != null ? Instant.ofEpochSecond(blockTimeLong) : null;
            String protocol = detectProtocol(payload);

            // --- Token transfer logic ---
            List<Map<String, Object>> tokenTransfers = getList(payload.get("tokenTransfers"));
            if (tokenTransfers == null || tokenTransfers.isEmpty()) {
                log.warn("[!] No tokenTransfers in webhook payload.");
                return;
            }

            // Only fetch top holders once per webhook event
            Set<String> topHolders = getTopHoldersSet(MONITORED_MINT);

            for (Map<String, Object> transfer : tokenTransfers) {
                String mint = (String) transfer.get("mint");
                if (!MONITORED_MINT.equals(mint)) continue; // skip unrelated mints

                String source = (String) transfer.get("fromUserAccount");
                String destination = (String) transfer.get("toUserAccount");
                String amount = transfer.get("tokenAmount") != null ? transfer.get("tokenAmount").toString() : "0";

                boolean sourceIsTop = source != null && topHolders.contains(source);
                boolean destIsTop = destination != null && topHolders.contains(destination);

                // If both source and destination are top holders, skip this event
                if (sourceIsTop && destIsTop) {
                    log.info("[SKIP] Both source and destination are top holders. Skipping event.");
                    continue;
                }

                // Decide buy/sell/transfer
                String walletAddress;
                String buySell;
                if (sourceIsTop && !destIsTop) {
                    walletAddress = source;
                    buySell = "Sell";
                } else if (!sourceIsTop && destIsTop) {
                    walletAddress = destination;
                    buySell = "Buy";
                } else {
                    walletAddress = destination;
                    buySell = "Transfer";
                }

                TransactionEvent tx = new TransactionEvent();
                tx.setSignature(signature);
                tx.setBlockTime(blockTime);
                tx.setWalletAddress(walletAddress);
                tx.setMint(mint);
                tx.setSource(source);
                tx.setDestination(destination);
                tx.setAmount(amount);
                tx.setProtocol(protocol);
                tx.setBuySell(buySell);

                transactionRepository.save(tx);

                log.info("[TX] {} | Time: {} | Mint: {} | Amount: {} | Source: {} | Dest: {} | Protocol: {} | {}",
                        signature, blockTime, mint, amount, source, destination, protocol, buySell);
            }
        } catch (Exception e) {
            log.error("[!] Error processing webhook payload: {}", e.getMessage(), e);
        }
    }

    private String detectProtocol(Map<String, Object> payload) {
        List<Map<String, Object>> instructions = getList(payload.get("instructions"));
        if (instructions == null) return "Unknown";
        for (Map<String, Object> instr : instructions) {
            String programId = (String) instr.get("programId");
            if (JUPITER_PROGRAM.equals(programId)) return "Jupiter";
            if (RAYDIUM_PROGRAM.equals(programId)) return "Raydium";
            if (ORCA_PROGRAM.equals(programId)) return "Orca";
        }
        return "Unknown";
    }





}

