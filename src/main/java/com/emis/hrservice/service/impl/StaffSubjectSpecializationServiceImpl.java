package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffSubjectSpecialization;
import com.emis.hrservice.dto.request.SubjectSpecializationRequest;
import com.emis.hrservice.dto.response.SubjectSpecializationResponse;
import com.emis.hrservice.enums.StaffCategory;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ValidationException;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffSubjectSpecializationRepository;
import com.emis.hrservice.service.StaffSubjectSpecializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffSubjectSpecializationServiceImpl implements StaffSubjectSpecializationService {

    private final StaffSubjectSpecializationRepository staffSubjectSpecializationRepository;
    private final StaffRepository staffRepository;
    private final TransactionalOperator transactionalOperator;
    @Override
    public Mono<SubjectSpecializationResponse> addSubjectSpecialization(String staffCode, String schoolCode,
                                             SubjectSpecializationRequest request, String requestId) {
    return staffRepository
        .findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException
                ("Staff not found with code %s in school %s".formatted(staffCode,  schoolCode))))
        .flatMap(
            staff -> {
                log.info("[{}] Adding subject specialization for staff {}", requestId, staffCode);
                if (staff.getStaffCategory() != StaffCategory.TEACHING) {
                    return Mono.error(
                            new ValidationException("Only teaching staff can have subject specializations")
                    );
                }
                if (Boolean.TRUE.equals(request.isMainTeachingSubject())){
                    return staffSubjectSpecializationRepository.existsByStaffIdAndIsMainTeachingSubjectTrueAndIsDeletedFalse(
                            staff.getStaffId())
                        .filter(exists -> !exists)
                            .switchIfEmpty( Mono.error(new ValidationException("Staff already has a main teaching subject")))
                    .thenReturn(staff);
                }
                return Mono.just(staff);
            })
            .flatMap(staff -> staffSubjectSpecializationRepository.findByStaffIdAndSubjectCodeAndIsDeletedFalse(
                       staff.getStaffId(), request.subjectCode())
                      .map(SubjectSpecializationResponse::from)
                      .switchIfEmpty(staffSubjectSpecializationRepository.save(buildStaffSubjectSpecialization(staff, request))
                      .map(SubjectSpecializationResponse::from)))
            .onErrorResume(DuplicateKeyException.class,
                    ex -> Mono.error(new ValidationException("Subject specialization already exists")));

    }

    private StaffSubjectSpecialization buildStaffSubjectSpecialization(
            Staff staff, SubjectSpecializationRequest request) {

    return StaffSubjectSpecialization.builder()
        .staffId(staff.getStaffId())
        .subjectCode(request.subjectCode())
        .subjectName(request.subjectName())
         .isMainTeachingSubject(request.isMainTeachingSubject())

        .proficiencyLevel(request.proficiencyLevel().name())
        .build();
    }
}
