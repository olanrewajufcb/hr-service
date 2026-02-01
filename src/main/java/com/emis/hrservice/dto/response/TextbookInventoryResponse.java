package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.TextbookInventory;

public record TextbookInventoryResponse(
    Long textbookId,
    Long schoolId,
    String bookType,
    String providedBy,
    String subjectArea,
    String gradeLevel,
    String title,
    String author,
    String publisher,
    String edition,
    String isbn,
    Integer publicationYear,
    String storageLocation
    ) {
    public static TextbookInventoryResponse from(TextbookInventory inventory) {
        return new TextbookInventoryResponse(
                inventory.getTextbookId(),
                inventory.getSchoolId(),
                inventory.getBookType(),
                inventory.getProvidedBy(),
                inventory.getSubjectArea(),
                inventory.getGradeLevel(),
                inventory.getTitle(),
                inventory.getAuthor(),
                inventory.getPublisher(),
                inventory.getEdition(),
                inventory.getIsbn(),
                inventory.getPublicationYear(),
                inventory.getStorageLocation()
        );
    }
}
