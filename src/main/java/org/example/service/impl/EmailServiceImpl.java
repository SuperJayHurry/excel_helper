package org.example.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String from;
    
    @Value("${spring.mail.password}")
    private String password;

    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    private void checkCredentials() {
        if (password != null && password.length() != 16) {
            System.err.println("===============================================================");
            System.err.println("WARNING: The configured email password has " + password.length() + " characters.");
            System.err.println("QQ Mail SMTP requires a 16-character AUTHORIZATION CODE.");
            System.err.println("It does NOT use your login password.");
            System.err.println("Please generate a code in QQ Mail Settings -> Accounts.");
            System.err.println("===============================================================");
        }
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        checkCredentials();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        } catch (Exception e) {
             System.err.println("Error sending email to " + to);
             if (e.getMessage().contains("535") || e.getMessage().contains("Authentication failed")) {
                 System.err.println(">>> AUTHENTICATION FAILED: Check your 16-char Authorization Code in application.properties <<<");
             }
             throw e;
        }
    }

    @Override
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment) {
        checkCredentials();
        MimeMessage message = emailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
            helper.addAttachment(file.getFilename(), file);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email with attachment", e);
        } catch (Exception e) {
            System.err.println("Error sending email with attachment to " + to);
            if (e.getMessage() != null && (e.getMessage().contains("535") || e.getMessage().contains("Authentication failed"))) {
                 System.err.println(">>> AUTHENTICATION FAILED: Check your 16-char Authorization Code in application.properties <<<");
            }
            throw e;
        }
    }
}
