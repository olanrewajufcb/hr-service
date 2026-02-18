package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.service.ReportFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Component
public class LocalReportStorage implements ReportFileStorage {

    private final ServiceConfigurationProperties properties;

    private static final Path ROOT = Paths.get("/var/emis/reports");

    @Override
    public Mono<String> upload(String fileName, byte[] content) {
    return Mono.fromCallable(
            () -> {
              Files.createDirectories(ROOT);
              Path path = ROOT.resolve(fileName);
              Files.write(path, content);
              return properties.getStorageBaseUrl() + fileName;
            })
        .subscribeOn(Schedulers.boundedElastic());
    }
}