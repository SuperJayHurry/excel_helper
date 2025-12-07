package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByTemplateTaskId(Long templateId);
    Optional<Submission> findByTemplateTaskIdAndSubmitterId(Long templateId, Long userId);
}

