package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.entity.TemplateRecipient;
import org.example.entity.TemplateTask;
import org.example.entity.User;
import org.example.entity.enums.RecipientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRecipientRepository extends JpaRepository<TemplateRecipient, Long> {
    List<TemplateRecipient> findByTemplateTaskId(Long templateId);
    List<TemplateRecipient> findByTemplateTaskIdAndStatus(Long templateId, RecipientStatus status);
    Optional<TemplateRecipient> findByTemplateTaskAndRecipient(TemplateTask templateTask, User recipient);
    List<TemplateRecipient> findByRecipientId(Long recipientId);
}

