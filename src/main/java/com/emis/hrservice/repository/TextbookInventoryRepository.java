package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.TextbookInventory;
import com.emis.hrservice.enums.BookType;
import com.emis.hrservice.enums.GradeLevel;
import com.emis.hrservice.enums.SubjectArea;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TextbookInventoryRepository extends ReactiveCrudRepository<TextbookInventory, Long> {

    Flux<TextbookInventory> findBySchoolId(Long schoolId);
    
    Flux<TextbookInventory> findBySchoolIdAndIsDeletedFalse(Long schoolId);
    
    Flux<TextbookInventory> findBySchoolIdAndBookTypeAndIsDeletedFalse(Long schoolId, BookType bookType);
    
    Flux<TextbookInventory> findBySchoolIdAndSubjectAreaAndIsDeletedFalse(Long schoolId, SubjectArea subjectArea);
    
    Flux<TextbookInventory> findBySchoolIdAndGradeLevelAndIsDeletedFalse(Long schoolId, GradeLevel gradeLevel);
    
    Flux<TextbookInventory> findBySchoolIdAndSubjectAreaAndGradeLevelAndIsDeletedFalse(
            Long schoolId, 
            SubjectArea subjectArea,
            GradeLevel gradeLevel);
    
    Mono<TextbookInventory> findBySchoolIdAndTitleAndSubjectAreaAndGradeLevelAndIsDeletedFalse(
            Long schoolId, 
            String title,
            SubjectArea subjectArea,
            GradeLevel gradeLevel);
    
    @Query("""
        SELECT * FROM hr_schema.textbook_inventory 
        WHERE school_id = $1 
        AND is_deleted = false 
        AND available_quantity < 5 
        AND status = 'ACTIVE'
        ORDER BY available_quantity ASC
    """)
    Flux<TextbookInventory> findLowStockBooks(Long schoolId);
    
    @Query("""
        SELECT * FROM hr_schema.textbook_inventory 
        WHERE school_id = $1 
        AND is_deleted = false 
        AND status = 'ACTIVE'
        AND (LOWER(title) LIKE LOWER(CONCAT('%', $2, '%')) 
             OR LOWER(author) LIKE LOWER(CONCAT('%', $2, '%')))
        ORDER BY subject_area, grade_level, title
        LIMIT 50
    """)
    Flux<TextbookInventory> searchBooks(Long schoolId, String searchTerm);
    
    @Query("UPDATE hr_schema.textbook_inventory SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE textbook_id = $1")
    Mono<Void> softDelete(Long textbookId);
}