package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.TextbookIssuance;
import com.emis.hrservice.enums.IssuanceStatus;
import com.emis.hrservice.enums.IssuedToType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface TextbookIssuanceRepository extends ReactiveCrudRepository<TextbookIssuance, Long> {


}