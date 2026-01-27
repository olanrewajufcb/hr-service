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

    @Column("book_type")
    private BookType bookType;

    @Column("provided_by")
    private ProvidedBy providedBy;

    @Column("subject_area")
    private SubjectArea subjectArea;

    @Column("grade_level")
    private GradeLevel gradeLevel;

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
    @Builder.Default
    private Integer totalQuantity = 0;

    @Column("available_quantity")
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column("issued_quantity")
    @Builder.Default
    private Integer issuedQuantity = 0;

    @Column("damaged_quantity")
    @Builder.Default
    private Integer damagedQuantity = 0;

    @Column("storage_location")
    private String storageLocation;

    @Column("last_audit_date")
    private LocalDate lastAuditDate;

    @Column("status")
    @Builder.Default
    private String status = "ACTIVE";

    @Column("is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}