package com.tokenwise.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class tokenAccountDTO {
    private String address;
    private String mint;
    private String owner;
    private long amount;
    private long delegated_amount;
    private boolean frozen;
}
