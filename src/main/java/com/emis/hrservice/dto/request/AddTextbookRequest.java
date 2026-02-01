package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.BookType;
import com.emis.hrservice.enums.GradeLevel;
import com.emis.hrservice.enums.ProvidedBy;
import com.emis.hrservice.enums.SubjectArea;

public record AddTextbookRequest(
    String title,
    String author,
    String publisher,
    String edition,
    String isbn,
    Integer publicationYear,
    BookType bookType,
    GradeLevel gradeLevel,
    SubjectArea subjectArea,
    ProvidedBy providedBy
    ) {}
