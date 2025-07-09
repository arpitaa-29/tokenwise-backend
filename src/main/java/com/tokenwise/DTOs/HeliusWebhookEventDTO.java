
package com.tokenwise.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class HeliusWebhookEventDTO {
    private String type;
    private long timestamp;
    private Description description;
    private Events events;

    @Data
    public static class Description {
        private String programName; // e.g., Jupiter, Raydium, Orca
    }

    @Data
    public static class Events {
        private List<TokenTransfer> tokenTransfers;
    }

    @Data
    public static class TokenTransfer {
        private String fromUserAccount;
        private String toUserAccount;
        private long tokenAmount;
        private String mint;
    }
}


