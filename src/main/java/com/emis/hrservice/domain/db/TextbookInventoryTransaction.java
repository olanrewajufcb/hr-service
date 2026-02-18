package com.emis.hrservice.domain.db;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "textbook_inventory_transactions", schema = "hr_schema")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TextbookInventoryTransaction {
    @Id
    private Long transactionId;
    private Long textbookId;
    private String transactionType;
    private int quantity;
    private String issuedToType;
    private String issuedToCode;
    private String issuedToName;
    private String reference;
    private String notes;
    private String performedBy;
    private LocalDateTime performedAt;

}
