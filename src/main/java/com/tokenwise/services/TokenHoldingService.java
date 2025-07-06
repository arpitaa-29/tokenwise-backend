package com.tokenwise.services;

import com.tokenwise.DTOs.TopHoldersResponseDTO;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TokenHoldingService {

    private final HeliusRpcService heliusRpcService;

    public TokenHoldingService(HeliusRpcService heliusRpcService) {
        this.heliusRpcService = heliusRpcService;
    }

    public List<TopHoldersResponseDTO> getTopTokenHolders(String mint, int topN, int decimals) {
        Map<String, Long> ownerToAmount = new HashMap<>();

        String cursor = null;
        int pageSize = 1000;
        boolean morePages = true;

        while (morePages) {
            var response = heliusRpcService.getTokenAccounts(mint, cursor, pageSize);
            if (response == null || response.getResult() == null) break;

            var accounts = response.getResult().getToken_accounts();
            if (accounts == null || accounts.isEmpty()) break;

            for (var account : accounts) {
                String owner = account.getOwner();
                long amount = account.getAmount();

                ownerToAmount.merge(owner, amount, Long::sum);
            }

            cursor = response.getResult().getCursor();
            morePages = cursor != null;

            if (ownerToAmount.size() >= topN * 2) {
                break;
            }
        }

        DecimalFormat df = new DecimalFormat("0.######");  // Up to 6 decimals

        return ownerToAmount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> {
                    String balanceFormatted = df.format(entry.getValue() / Math.pow(10, decimals));
                    return new TopHoldersResponseDTO(
                            entry.getKey(),
                            entry.getValue(),
                            balanceFormatted
                    );
                })
                .collect(Collectors.toList());
    }
}
