package com.tokenwise.controllers;

import com.tokenwise.DTOs.WalletAmountDTO;
import com.tokenwise.DTOs.WalletFrequencyDTO;
import com.tokenwise.models.TransactionEvent;
import com.tokenwise.services.InsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/insights")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    // 1. Buy/Sell Counts (filtered)
    @GetMapping("/buys-vs-sells")
    public Map<String, Long> getBuySellStats(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end
    ) {
        return insightService.getBuySellStatsByMintAndTimeRange(mint, window, start, end);
    }

    // 2. Net Direction (filtered)
    @GetMapping("/net-direction")
    public Map<String, String> getNetDirection(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end
    ) {
        return insightService.getNetDirectionByMintAndTimeRange(mint, window, start, end);
    }

    // 4. Protocol usage breakdown (filtered)
    @GetMapping("/protocol-usage")
    public Map<String, Long> getProtocolUsage(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end
    ) {
        return insightService.getProtocolUsageByMintAndTimeRange(mint, window, start, end);
    }

    // 5. Export CSV (filtered)
    @GetMapping("/transactions/export")
    public ResponseEntity<String> exportTransactionsCsv(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end
    ) {
        List<TransactionEvent> txs = insightService.getTransactionsByMintAndTimeRange(mint, window, start, end);
        String csv = insightService.toCSV(txs);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv);
    }

    // 6. Get Transactions (filtered, JSON)
    @GetMapping("/transactions")
    public List<TransactionEvent> getTransactions(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end
    ) {
        return insightService.getTransactionsByMintAndTimeRange(mint, window, start, end);
    }

    // 7. Transaction Summary (number, value, unique wallets)
    @GetMapping("/summary")
    public Map<String, Object> getTransactionSummary(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end
    ) {
        return insightService.getTransactionSummary(mint, window, start, end);
    }
    // 8. Top Wallets by Amount
    @GetMapping("/top-wallets")
    public List<WalletAmountDTO> getTopWalletsByAmount(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<WalletAmountDTO> entries = insightService.getTopWalletsByAmount(mint, window, start, end, limit);
        return entries == null ? Collections.emptyList() : entries;
    }

    // 9. Most Frequent Wallets (by transaction count)
    @GetMapping("/frequent-wallets")
    public List<WalletFrequencyDTO> getMostFrequentWallets(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<WalletFrequencyDTO> entries = insightService.getMostFrequentWalletsByMintAndTimeRange(mint, window, start, end);
        return entries.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }


    // 11. Export Transactions as JSON
    @GetMapping("/transactions/export/json")
    public ResponseEntity<List<TransactionEvent>> exportTransactionsJson(
            @RequestParam String mint,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end
    ) {
        List<TransactionEvent> txs = insightService.getTransactionsByMintAndTimeRange(mint, window, start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(txs);
    }


}
