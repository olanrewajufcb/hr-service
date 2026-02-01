package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.AddTextbookRequest;
import com.emis.hrservice.dto.response.AddTextbookResponse;
import com.emis.hrservice.dto.response.TextbookInventoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface TextbookManagementService {

    Mono<AddTextbookResponse> addTextbookToInventory(AddTextbookRequest request,
                                                    String schoolCode, String requestId);

    Mono<Page<TextbookInventoryResponse>> retrieveTextbooksFromInventory(
            String schoolCode, Pageable pageable, String requestId);
}
