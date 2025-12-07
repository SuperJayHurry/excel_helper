package org.example.service;

import java.util.List;
import org.example.dto.TemplateForm;
import org.example.entity.AggregationResult;
import org.example.entity.TemplateRecipient;
import org.example.entity.TemplateTask;
import org.example.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface TemplateService {

    TemplateTask createTask(TemplateForm form, MultipartFile templateFile, User admin);

    List<TemplateTask> findAll();

    TemplateTask getById(Long id);

    List<TemplateRecipient> findRecipients(Long templateId);

    void remindPending(Long templateId, String message);

    AggregationResult aggregate(Long templateId);

    AggregationResult latestAggregation(Long templateId);
}

