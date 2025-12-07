package org.example.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FilenameUtils;
import org.example.config.StorageProperties;
import org.example.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path root;
    private final Path templateDir;
    private final Path submissionDir;
    private final Path aggregationDir;

    public FileStorageServiceImpl(StorageProperties properties) throws IOException {
        this.root = Paths.get(properties.getRoot()).toAbsolutePath().normalize();
        this.templateDir = root.resolve(properties.getTemplates());
        this.submissionDir = root.resolve(properties.getSubmissions());
        this.aggregationDir = root.resolve(properties.getAggregations());
        Files.createDirectories(templateDir);
        Files.createDirectories(submissionDir);
        Files.createDirectories(aggregationDir);
    }

    @Override
    public String storeTemplate(MultipartFile file, String taskName) {
        return storeFile(file, templateDir, sanitize(taskName));
    }

    @Override
    public String storeSubmission(MultipartFile file, Long templateId, Long userId) {
        Path targetDir = submissionDir.resolve("task-" + templateId).resolve("user-" + userId);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建存储目录", e);
        }
        return storeFile(file, targetDir, "submission");
    }

    @Override
    public Path createAggregationFile(String templateName) {
        String safeName = sanitize(templateName) + "-汇总-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        Path targetPath = aggregationDir.resolve(safeName);
        try {
            Files.createDirectories(aggregationDir);
        } catch (IOException e) {
            throw new IllegalStateException("创建汇总目录失败", e);
        }
        return targetPath;
    }

    @Override
    public String toRelative(Path absolutePath) {
        return root.relativize(absolutePath).toString().replace("\\", "/");
    }

    @Override
    public Path resolvePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        return root.resolve(relativePath).normalize();
    }

    private String storeFile(MultipartFile file, Path targetDir, String prefix) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String safeName = prefix + "-" + timestamp + (StringUtils.hasText(extension) ? "." + extension : ".xlsx");
        Path targetPath = targetDir.resolve(safeName);
        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetPath.toFile());
            return root.relativize(targetPath).toString().replace("\\", "/");
        } catch (IOException e) {
            throw new IllegalStateException("保存文件失败", e);
        }
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\-]", "_");
    }
}

