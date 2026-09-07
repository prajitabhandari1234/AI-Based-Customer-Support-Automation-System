package com.cqu.coit13230.AIBasedCustomerSupport.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseEntry, Long> {
    List<KnowledgeBaseEntry> findByActiveTrueOrderByCategoryAscQuestionPatternAsc();
    List<KnowledgeBaseEntry> findAllByOrderByCategoryAscQuestionPatternAsc();
}
