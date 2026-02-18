package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.TextbookInventoryTransaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TextbookTransactionRepository extends ReactiveCrudRepository<TextbookInventoryTransaction, Long> {}
