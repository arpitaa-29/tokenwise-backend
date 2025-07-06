package com.tokenwise.controllers;

import com.tokenwise.DTOs.TopHoldersResponseDTO;
import com.tokenwise.services.TokenHoldingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final TokenHoldingService tokenHoldingService;

    public TokenController(TokenHoldingService tokenHoldingService) {
        this.tokenHoldingService = tokenHoldingService;
    }

    @GetMapping("/{mint}/top-holders")
    public ResponseEntity<List<TopHoldersResponseDTO>> getTopHolders(
            @PathVariable String mint,
            @RequestParam(defaultValue = "60") int limit) {

        if (limit <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<TopHoldersResponseDTO> topHolders = tokenHoldingService.getTopTokenHolders(mint, limit,6);
        return ResponseEntity.ok(topHolders);
    }
}
