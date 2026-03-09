package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@ExtendWith(MockitoExtension.class)
class LocalReportStorageTest {

    @Mock
    private ServiceConfigurationProperties properties;

    @TempDir
    Path tempDir;

    private LocalReportStorage localReportStorage;

    @BeforeEach
    void setUp() {
        // We override getRoot to use a temporary directory for testing
        localReportStorage = new LocalReportStorage(properties) {
            @Override
            protected Path getRoot() {
                return tempDir;
            }
        };
    }

    @Test
    void upload_ShouldReturnUrlAndWriteFile() throws IOException {
        String fileName = "test-report.pdf";
        byte[] content = "test content".getBytes();
        String baseUrl = "http://localhost:8080/reports/";
        
        when(properties.getStorageBaseUrl()).thenReturn(baseUrl);

        StepVerifier.create(localReportStorage.upload(fileName, content))
                .expectNext(baseUrl + fileName)
                .verifyComplete();

        // Verify file was written to the tempDir
        Path filePath = tempDir.resolve(fileName);
        assertTrue(Files.exists(filePath));
        assertArrayEquals(content, Files.readAllBytes(filePath));
    }
}
