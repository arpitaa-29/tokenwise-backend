package com.tokenwise.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class tokenAccountDTO {
    private String address;
    private String mint;
    private String owner;
    private long amount;
    private long delegated_amount;
    private boolean frozen;
}
