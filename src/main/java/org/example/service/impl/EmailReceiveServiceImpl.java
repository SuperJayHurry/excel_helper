package org.example.service.impl;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.search.FlagTerm;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.example.entity.Submission;
import org.example.entity.TemplateRecipient;
import org.example.entity.TemplateTask;
import org.example.entity.User;
import org.example.entity.enums.RecipientStatus;
import org.example.repository.SubmissionRepository;
import org.example.repository.TemplateRecipientRepository;
import org.example.repository.TemplateTaskRepository;
import org.example.repository.UserRepository;
import org.example.service.EmailReceiveService;
import org.example.service.FileStorageService;
import org.example.service.SubmissionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;

@Service
public class EmailReceiveServiceImpl implements EmailReceiveService {

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    private final TemplateTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SubmissionService submissionService;

    public EmailReceiveServiceImpl(TemplateTaskRepository taskRepository,
                                   UserRepository userRepository,
                                   SubmissionService submissionService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.submissionService = submissionService;
    }

    @Override
    @Transactional
    public int receiveSubmissions(Long taskId) {
        TemplateTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        int processCount = 0;
        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imaps.host", "imap.qq.com");
            props.setProperty("mail.imaps.port", "993");
            props.setProperty("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect("imap.qq.com", username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Fetch unseen messages or all? Better to fetch all recent ones and check if processed?
            // For simplicity, let's look at UNSEEN first, or just look at last N messages.
            // Using FlagTerm to find unread messages is safer to avoid processing same mail twice.
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (Message message : messages) {
                if (processMessage(message, task)) {
                    processCount++;
                    // Mark as seen/read
                    message.setFlag(Flags.Flag.SEEN, true);
                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to receive emails", e);
        }
        return processCount;
    }

    private boolean processMessage(Message message, TemplateTask task) throws Exception {
        String from = ((InternetAddress) message.getFrom()[0]).getAddress();
        
        // 1. Identify User by Email
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(from))
                .findFirst()
                .orElse(null);

        if (user == null) {
            System.out.println("Ignored email from unknown user: " + from);
            return false;
        }

        // 2. Check if Subject matches Task? 
        // Strategy: 
        // A. Strict Match: Subject contains Task Name.
        // B. Fallback: If subject contains "成绩汇总" or "submission" and user has this pending task, maybe accept?
        // Current: Strict match on Task Name.
        String subject = message.getSubject();
        
        // Normalize subject and task name for comparison (remove spaces, case insensitive)
        String normalizedSubject = subject != null ? subject.replaceAll("\\s+", "").toLowerCase() : "";
        String normalizedTaskName = task.getName().replaceAll("\\s+", "").toLowerCase();

        if (subject == null || !normalizedSubject.contains(normalizedTaskName)) {
             System.out.println("Subject mismatch for user " + from + ": " + subject + " (Expected to contain: " + task.getName() + ")");
             return false;
        }

        // 3. Find Attachment
        List<MultipartFile> attachments = extractAttachments(message);
        if (attachments.isEmpty()) {
            return false;
        }

        // 4. Save Submission
        // Only take the first Excel file found
        for (MultipartFile file : attachments) {
             if (file.getOriginalFilename().endsWith(".xlsx") || file.getOriginalFilename().endsWith(".xls")) {
                 submissionService.submit(task.getId(), user, file);
                 System.out.println("Processed submission from " + from);
                 return true;
             }
        }
        
        return false;
    }

    private List<MultipartFile> extractAttachments(Message message) throws Exception {
        List<MultipartFile> attachments = new ArrayList<>();
        Object content = message.getContent();

        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) || 
                    (bodyPart.getFileName() != null && !bodyPart.getFileName().isEmpty())) {
                    
                    String fileName = MimeUtility.decodeText(bodyPart.getFileName());
                    InputStream is = bodyPart.getInputStream();
                    byte[] bytes = IOUtils.toByteArray(is);
                    attachments.add(new MockMultipartFile(fileName, fileName, bodyPart.getContentType(), bytes));
                }
            }
        }
        return attachments;
    }
}

