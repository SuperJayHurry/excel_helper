package org.example.controller;

import java.io.IOException;
import java.nio.file.Files;
import org.example.service.FileStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/files")
    public ResponseEntity<InputStreamResource> download(@RequestParam String path) throws IOException {
        var file = fileStorageService.resolvePath(path);
        if (file == null || !Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        String filename = file.getFileName().toString();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String contentType = Files.probeContentType(file);
        if (contentType != null) {
            mediaType = MediaType.parseMediaType(contentType);
        }
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(file));
        
        // Encode filename for URL safety, but keep spaces as %20 instead of +
        String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                                    .replace("+", "%20");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentType(mediaType)
                .body(resource);
    }
}
