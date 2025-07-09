package com.tokenwise.services;

import com.tokenwise.DTOs.WalletAmountDTO;
import com.tokenwise.DTOs.WalletFrequencyDTO;
import com.tokenwise.models.TransactionEvent;
import com.tokenwise.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import static com.tokenwise.utils.Helper.resolveTimeRange;

@Service
public class InsightService {

    private final TransactionRepository transactionRepository;

    public InsightService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }



    // Core fetcher (used by all filtered analytics)
    public List<TransactionEvent> getTransactionsByMintAndTimeRange(String mint, String window, Long start, Long end) {
        long[] range = resolveTimeRange(window, start, end);
        Instant startTime = Instant.ofEpochSecond(range[0]);
        Instant endTime = Instant.ofEpochSecond(range[1]);
        return transactionRepository.findByMintAndBlockTimeBetween(mint, startTime, endTime);
    }

    // Buy/Sell Stats (filtered)
    public Map<String, Long> getBuySellStatsByMintAndTimeRange(String mint, String window, Long start, Long end) {
        List<TransactionEvent> filtered = getTransactionsByMintAndTimeRange(mint, window, start, end);
        long buyCount = filtered.stream().filter(tx -> "Buy".equalsIgnoreCase(tx.getBuySell())).count();
        long sellCount = filtered.stream().filter(tx -> "Sell".equalsIgnoreCase(tx.getBuySell())).count();
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("buyCount", buyCount);
        result.put("sellCount", sellCount);
        return result;
    }

    // Net Direction (filtered)
    public Map<String, String> getNetDirectionByMintAndTimeRange(String mint, String window, Long start, Long end) {
        List<TransactionEvent> filtered = getTransactionsByMintAndTimeRange(mint, window, start, end);
        long buyCount = filtered.stream().filter(tx -> "Buy".equalsIgnoreCase(tx.getBuySell())).count();
        long sellCount = filtered.stream().filter(tx -> "Sell".equalsIgnoreCase(tx.getBuySell())).count();
        String netDirection = buyCount > sellCount ? "Buy-heavy" : (buyCount < sellCount ? "Sell-heavy" : "Neutral");
        Map<String, String> result = new LinkedHashMap<>();
        result.put("netDirection", netDirection);
        return result;
    }

    // Most Frequent Wallets with frequency count
    public List<WalletFrequencyDTO> getMostFrequentWalletsByMintAndTimeRange(String mint, String window, Long start, Long end) {
        List<TransactionEvent> filtered = getTransactionsByMintAndTimeRange(mint, window, start, end);
        Map<String, Long> walletActivity = filtered.stream()
                .collect(Collectors.groupingBy(TransactionEvent::getWalletAddress, Collectors.counting()));

        return walletActivity.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(entry -> new WalletFrequencyDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> b.getFrequency().compareTo(a.getFrequency())) // Sort by frequency descending
                .collect(Collectors.toList());
    }

    // Protocol Usage (filtered)
    public Map<String, Long> getProtocolUsageByMintAndTimeRange(String mint, String window, Long start, Long end) {
        List<TransactionEvent> filtered = getTransactionsByMintAndTimeRange(mint, window, start, end);
        return filtered.stream()
                .collect(Collectors.groupingBy(
                        tx -> Optional.ofNullable(tx.getProtocol()).orElse("Other"),
                        Collectors.counting()
                ));
    }

    // CSV Export
    public String toCSV(List<TransactionEvent> txs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Signature,BlockTime,BuySell,Source,Destination,Amount,Protocol,WalletAddress\n");
        for (TransactionEvent tx : txs) {
            sb.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    tx.getSignature(),
                    tx.getBlockTime(),
                    tx.getBuySell(),
                    tx.getSource(),
                    tx.getDestination(),
                    tx.getAmount(),
                    tx.getProtocol(),
                    tx.getWalletAddress()
            ));
        }
        return sb.toString();
    }

    public Map<String, Object> getTransactionSummary(String mint, String window, Long start, Long end) {
        List<TransactionEvent> filtered = getTransactionsByMintAndTimeRange(mint, window, start, end);
        long transactionCount = filtered.size();
        double totalValue = filtered.stream()
                .mapToDouble(tx -> {
                    try {
                        return Double.parseDouble(tx.getAmount());
                    } catch (NumberFormatException e) {
                        return 0.0; // or handle/log as needed
                    }
                })
                .sum();

        long uniqueWallets = filtered.stream().map(TransactionEvent::getWalletAddress).distinct().count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("TransactionCount", transactionCount);
        result.put("TotalValue", totalValue);
        result.put("UniqueWallets", uniqueWallets);
        return result;
    }

    public List<WalletAmountDTO> getTopWalletsByAmount(String mint, String window, Long start, Long end, int limit) {
        List<TransactionEvent> filtered = getTransactionsByMintAndTimeRange(mint, window, start, end);
        Map<String, Double> totals = filtered.stream()
                .collect(Collectors.groupingBy(
                        TransactionEvent::getWalletAddress,
                        Collectors.summingDouble(te -> Double.parseDouble(te.getAmount()))
                ));
        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new WalletAmountDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }


}
