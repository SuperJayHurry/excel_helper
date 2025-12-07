package org.example.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.Submission;
import org.example.entity.TemplateRecipient;
import org.example.entity.TemplateTask;
import org.example.entity.User;
import org.example.entity.enums.RecipientStatus;
import org.example.repository.SubmissionRepository;
import org.example.repository.TemplateRecipientRepository;
import org.example.repository.TemplateTaskRepository;
import org.example.service.FileStorageService;
import org.example.service.SubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final TemplateRecipientRepository recipientRepository;
    private final TemplateTaskRepository templateTaskRepository;
    private final SubmissionRepository submissionRepository;
    private final FileStorageService fileStorageService;

    public SubmissionServiceImpl(TemplateRecipientRepository recipientRepository,
                                 TemplateTaskRepository templateTaskRepository,
                                 SubmissionRepository submissionRepository,
                                 FileStorageService fileStorageService) {
        this.recipientRepository = recipientRepository;
        this.templateTaskRepository = templateTaskRepository;
        this.submissionRepository = submissionRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<TemplateRecipient> assignmentsFor(Long userId) {
        return recipientRepository.findByRecipientId(userId);
    }

    @Override
    @Transactional
    public Submission submit(Long templateId, User user, MultipartFile file) {
        TemplateTask task = templateTaskRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        TemplateRecipient recipient = recipientRepository.findByTemplateTaskAndRecipient(task, user)
                .orElseThrow(() -> new IllegalArgumentException("该教师不在发布对象中"));

        int rowCount = countRows(file);
        String storedPath = fileStorageService.storeSubmission(file, templateId, user.getId());
        Submission submission = submissionRepository
                .findByTemplateTaskIdAndSubmitterId(templateId, user.getId())
                .orElseGet(Submission::new);
        submission.setTemplateTask(task);
        submission.setSubmitter(user);
        submission.setFileName(file.getOriginalFilename());
        submission.setFilePath(storedPath);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setTotalRows(rowCount);
        Submission saved = submissionRepository.save(submission);

        recipient.setStatus(RecipientStatus.SUBMITTED);
        recipientRepository.save(recipient);
        return saved;
    }

    @Override
    public List<Submission> findByTemplate(Long templateId) {
        return submissionRepository.findByTemplateTaskId(templateId);
    }

    private int countRows(MultipartFile file) {
        try (InputStream stream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(stream)) {
            var sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            return sheet != null ? sheet.getPhysicalNumberOfRows() : 0;
        } catch (IOException e) {
            return 0;
        }
    }
}

