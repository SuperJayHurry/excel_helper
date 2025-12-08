package org.example.service.impl;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.example.dto.TemplateForm;
import org.example.entity.AggregationResult;
import org.example.entity.TemplateRecipient;
import org.example.entity.TemplateTask;
import org.example.entity.User;
import org.example.entity.enums.Department;
import org.example.entity.enums.RecipientStatus;
import org.example.entity.enums.UserRole;
import org.example.repository.AggregationResultRepository;
import org.example.repository.ReminderLogRepository;
import org.example.repository.SubmissionRepository;
import org.example.repository.TemplateRecipientRepository;
import org.example.repository.TemplateTaskRepository;
import org.example.repository.UserRepository;
import org.example.service.EmailService;
import org.example.service.FileStorageService;
import org.example.service.TemplateService;
import org.example.util.ExcelAggregationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TemplateServiceImpl implements TemplateService {

    private final TemplateTaskRepository templateTaskRepository;
    private final TemplateRecipientRepository recipientRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ReminderLogRepository reminderLogRepository;
    private final SubmissionRepository submissionRepository;
    private final AggregationResultRepository aggregationResultRepository;
    private final EmailService emailService;

    public TemplateServiceImpl(TemplateTaskRepository templateTaskRepository,
                               TemplateRecipientRepository recipientRepository,
                               UserRepository userRepository,
                               FileStorageService fileStorageService,
                               ReminderLogRepository reminderLogRepository,
                               SubmissionRepository submissionRepository,
                               AggregationResultRepository aggregationResultRepository,
                               EmailService emailService) {
        this.templateTaskRepository = templateTaskRepository;
        this.recipientRepository = recipientRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.reminderLogRepository = reminderLogRepository;
        this.submissionRepository = submissionRepository;
        this.aggregationResultRepository = aggregationResultRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public TemplateTask createTask(TemplateForm form, MultipartFile templateFile, User admin) {
        String storedPath = fileStorageService.storeTemplate(templateFile, form.getName());
        TemplateTask task = new TemplateTask();
        task.setName(form.getName());
        task.setDescription(form.getDescription());
        task.setDeadline(form.getDeadline());
        task.setTemplateFileName(templateFile.getOriginalFilename());
        task.setTemplateFilePath(storedPath);
        task.setCreatedBy(admin);
        TemplateTask savedTask = templateTaskRepository.save(task);

        List<User> recipients = resolveRecipients(form);
        for (User recipient : recipients) {
            TemplateRecipient templateRecipient = new TemplateRecipient();
            templateRecipient.setTemplateTask(savedTask);
            templateRecipient.setRecipient(recipient);
            recipientRepository.save(templateRecipient);

            // Send email notification
            if (recipient.getEmail() != null && !recipient.getEmail().isEmpty()) {
                try {
                    String subject = "新任务通知: " + savedTask.getName();
                    String content = "各位老师好，\n\n您有一个新的任务需要处理。\n\n" +
                            "任务名称: " + savedTask.getName() + "\n" +
                            "任务描述: " + savedTask.getDescription() + "\n" +
                            "截止日期: " + savedTask.getDeadline() + "\n\n" +
                            "请查看附件中的模版，填写后上传到系统。";
                    
                    Path absolutePath = fileStorageService.resolvePath(storedPath).toAbsolutePath();
                    emailService.sendMessageWithAttachment(recipient.getEmail(), subject, content, absolutePath.toString());
                } catch (Exception e) {
                    // Log error but don't fail the transaction
                    System.err.println("Failed to send email to " + recipient.getEmail() + ": " + e.getMessage());
                }
            }
        }
        return savedTask;
    }

    private List<User> resolveRecipients(TemplateForm form) {
        if ("ALL".equalsIgnoreCase(form.getTargetScope())) {
            return userRepository.findAllByRole(UserRole.TEACHER);
        }
        if ("CUSTOM".equalsIgnoreCase(form.getTargetScope()) && form.getTeacherIds() != null) {
            return userRepository.findAllById(form.getTeacherIds());
        }
        try {
            Department department = Department.valueOf(form.getTargetScope().toUpperCase(Locale.ROOT));
            return userRepository.findAllByRoleAndDepartment(UserRole.TEACHER, department);
        } catch (IllegalArgumentException ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<TemplateTask> findAll() {
        return templateTaskRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public TemplateTask getById(Long id) {
        return templateTaskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
    }

    @Override
    public List<TemplateRecipient> findRecipients(Long templateId) {
        return recipientRepository.findByTemplateTaskId(templateId);
    }

    @Override
    @Transactional
    public void remindPending(Long templateId, String message) {
        List<TemplateRecipient> pendingRecipients = recipientRepository.findByTemplateTaskId(templateId)
                .stream()
                .filter(recipient -> recipient.getStatus() != RecipientStatus.SUBMITTED)
                .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        pendingRecipients.forEach(recipient -> {
            recipient.setStatus(RecipientStatus.REMINDED);
            recipient.setLastReminderAt(now);
            recipient.setReminderCount(recipient.getReminderCount() + 1);
            recipientRepository.save(recipient);
        });
        pendingRecipients.forEach(recipient -> {
            var log = new org.example.entity.ReminderLog();
            log.setTemplateTask(recipient.getTemplateTask());
            log.setRecipient(recipient.getRecipient());
            log.setMessage(message);
            reminderLogRepository.save(log);
        });
    }

    @Override
    @Transactional
    public AggregationResult aggregate(Long templateId) {
        TemplateTask task = getById(templateId);
        List<Path> sourceFiles = submissionRepository.findByTemplateTaskId(templateId).stream()
                .map(submission -> fileStorageService.resolvePath(submission.getFilePath()))
                .filter(path -> path != null)
                .collect(Collectors.toList());
        if (sourceFiles.isEmpty()) {
            throw new IllegalStateException("暂无任何教师上传，无法汇总");
        }
        Path aggregationPath = fileStorageService.createAggregationFile(task.getName());
        int mergedRows = ExcelAggregationUtil.mergeFirstSheet(sourceFiles, aggregationPath);
        AggregationResult result = new AggregationResult();
        result.setTemplateTask(task);
        result.setFileName(aggregationPath.getFileName().toString());
        result.setFilePath(fileStorageService.toRelative(aggregationPath));
        result.setTotalRows(mergedRows);
        result.setGeneratedAt(LocalDateTime.now());
        return aggregationResultRepository.save(result);
    }

    @Override
    public AggregationResult latestAggregation(Long templateId) {
        return aggregationResultRepository.findTopByTemplateTaskIdOrderByCreatedAtDesc(templateId).orElse(null);
    }
}

