package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;

/**
 * Provides database access operations for {@link KnowledgeBaseEntry}
 * entities within the AI-based customer support system.
 *
 * <p>
 * This repository extends Spring Data JPA's {@link JpaRepository},
 * which provides standard persistence operations such as creating,
 * retrieving, updating, and deleting knowledge base entries.
 * </p>
 *
 * <p>
 * It also defines project-specific query methods for retrieving
 * knowledge base entries in an organised order and for retrieving
 * only entries that are currently active.
 * </p>
 */
public interface KnowledgeBaseEntryRepository extends JpaRepository<KnowledgeBaseEntry, Long> {

    /**
     * Retrieves all knowledge base entries ordered first by category
     * and then by question pattern in ascending order.
     *
     * <p>
     * Spring Data JPA automatically derives the required database
     * query from the method name.
     * </p>
     *
     * @return a list of all knowledge base entries ordered by category
     *         and question pattern in ascending order
     */
    List<KnowledgeBaseEntry> findAllByOrderByCategoryAscQuestionPatternAsc();

    /**
     * Retrieves all active knowledge base entries ordered first by
     * category and then by question pattern in ascending order.
     *
     * <p>
     * Only entries where the {@code active} field is {@code true}
     * are included. Spring Data JPA automatically derives the required
     * database query from the method name.
     * </p>
     *
     * @return a list of active knowledge base entries ordered by category
     *         and question pattern in ascending order
     */
    List<KnowledgeBaseEntry> findByActiveTrueOrderByCategoryAscQuestionPatternAsc();

}