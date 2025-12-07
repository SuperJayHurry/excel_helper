package org.example.service;

import java.util.List;
import org.example.entity.Submission;
import org.example.entity.TemplateRecipient;
import org.example.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {

    List<TemplateRecipient> assignmentsFor(Long userId);

    Submission submit(Long templateId, User user, MultipartFile file);

    List<Submission> findByTemplate(Long templateId);
}

