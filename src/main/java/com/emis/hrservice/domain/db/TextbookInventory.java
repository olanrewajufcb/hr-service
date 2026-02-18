package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.BookType;
import com.emis.hrservice.enums.GradeLevel;
import com.emis.hrservice.enums.ProvidedBy;
import com.emis.hrservice.enums.SubjectArea;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "textbook_inventory", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextbookInventory {

    @Id
    private Long textbookId;

    @Column("school_id")
    private Long schoolId;

    private String schoolCode;

    @Column("book_type")
    private String bookType;

    @Column("provided_by")
    private String providedBy;

    @Column("subject_area")
    private String subjectArea;

    @Column("grade_level")
    private String gradeLevel;

    @Column("title")
    private String title;

    @Column("author")
    private String author;

    @Column("publisher")
    private String publisher;

    @Column("edition")
    private String edition;

    @Column("isbn")
    private String isbn;

    @Column("publication_year")
    private Integer publicationYear;

    @Column("total_quantity")
    private Integer totalQuantity;

    @Column("available_quantity")
    private Integer availableQuantity;

    @Column("issued_quantity")
    private Integer issuedQuantity;

    @Column("damaged_quantity")
    private Integer damagedQuantity;

    @Column("storage_location")
    private String storageLocation;

    @Column("last_audit_date")
    private LocalDate lastAuditDate;

    @Column("status")
    private String status;

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