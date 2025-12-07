package org.example.repository;

import java.util.List;
import org.example.entity.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
    List<ReminderLog> findByTemplateTaskId(Long templateId);
}

