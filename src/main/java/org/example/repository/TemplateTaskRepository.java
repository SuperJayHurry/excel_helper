package org.example.repository;

import java.util.List;
import org.example.entity.TemplateTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateTaskRepository extends JpaRepository<TemplateTask, Long> {
    List<TemplateTask> findAllByOrderByCreatedAtDesc();
}

