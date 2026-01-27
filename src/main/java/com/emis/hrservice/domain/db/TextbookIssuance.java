package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.BookCondition;
import com.emis.hrservice.enums.IssuanceStatus;
import com.emis.hrservice.enums.IssuedToType;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "textbook_issuance", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextbookIssuance {

    @Id
    private Long issuanceId;

    @Column("textbook_id")
    private Long textbookId;

    @Column("school_id")
    private Long schoolId;

    @Column("issued_to_type")
    private IssuedToType issuedToType;

    @Column("issued_to_id")
    private Long issuedToId;

    @Column("issued_to_name")
    private String issuedToName;

    @Column("quantity_issued")
    private Integer quantityIssued;

    @Column("issuance_date")
    private LocalDate issuanceDate;

    @Column("expected_return_date")
    private LocalDate expectedReturnDate;

    @Column("actual_return_date")
    private LocalDate actualReturnDate;

    @Column("issued_condition")
    private BookCondition issuedCondition;

    @Column("returned_condition")
    private BookCondition returnedCondition;

    @Column("issued_by_staff_id")
    private Long issuedByStaffId;

    @Column("received_by")
    private String receivedBy;

    @Column("notes")
    private String notes;

    @Column("issuance_status")
    private IssuanceStatus issuanceStatus;

    @Column("is_deleted")
    private Boolean isDeleted;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}