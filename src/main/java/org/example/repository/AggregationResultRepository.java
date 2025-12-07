package org.example.repository;

import java.util.Optional;
import org.example.entity.AggregationResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AggregationResultRepository extends JpaRepository<AggregationResult, Long> {
    Optional<AggregationResult> findTopByTemplateTaskIdOrderByCreatedAtDesc(Long templateId);
}

