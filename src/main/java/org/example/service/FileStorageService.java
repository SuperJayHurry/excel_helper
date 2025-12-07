package org.example.service;

import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeTemplate(MultipartFile file, String taskName);

    String storeSubmission(MultipartFile file, Long templateId, Long userId);

    Path createAggregationFile(String templateName);

    String toRelative(Path absolutePath);

    Path resolvePath(String relativePath);
}

