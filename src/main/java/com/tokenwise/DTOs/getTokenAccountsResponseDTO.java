package com.tokenwise.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class getTokenAccountsResponseDTO {
    private Result result;

    @Data
    public static class Result {
        private List<tokenAccountDTO> token_accounts;
        private String cursor;
    }
}

