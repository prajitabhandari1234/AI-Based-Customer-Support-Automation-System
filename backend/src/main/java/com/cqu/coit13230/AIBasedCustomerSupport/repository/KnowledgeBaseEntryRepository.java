package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;

/**
 * Provides database access for knowledge base entries.
 * Spring Data JPA provides the standard CRUD operations while this interface adds project-specific lookups.
 */
public interface KnowledgeBaseEntryRepository extends JpaRepository<KnowledgeBaseEntry, Long> {

    List<KnowledgeBaseEntry> findAllByOrderByCategoryAscQuestionPatternAsc();

    List<KnowledgeBaseEntry> findByActiveTrueOrderByCategoryAscQuestionPatternAsc();
}
