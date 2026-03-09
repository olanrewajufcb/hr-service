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


    @Query("""
        SELECT * FROM hr_schema.textbook_inventory 
        WHERE school_id = $1 
        AND is_deleted = false
        ORDER BY subject_area, grade_level, title
        LIMIT $2 OFFSET $3
    """)
    Flux<TextbookInventory> findBySchoolIdAndIsDeletedFalse(Long schoolId, int size, long offset);
    Mono<Integer> countBySchoolIdAndIsDeletedFalse(Long schoolId);


    @Query("""
        SELECT * FROM hr_schema.textbook_inventory 
        WHERE school_id = $1 
        AND title = $2
        AND subject_area = $3
        AND edition = $4
        AND grade_level = $5
        AND is_deleted = false
    """)
    Mono<TextbookInventory> findBySchoolIdAndTitleAndSubjectAreaAndEditionAndGradeLevelAndIsDeletedFalse(
            Long schoolId, 
            String title,
            String subjectArea,
            String edition,
            String gradeLevel);

}