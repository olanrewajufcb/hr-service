package com.emis.hrservice.service;

import reactor.core.publisher.Mono;

public interface ReportFileStorage {
    Mono<String> upload(String fileName, byte[] content);
}