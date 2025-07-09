package com.tokenwise.repositories;

import com.tokenwise.models.TransactionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEvent, Long> {
    List<TransactionEvent> findByMintAndBlockTimeBetween(String mint, Instant start, Instant end);




}


