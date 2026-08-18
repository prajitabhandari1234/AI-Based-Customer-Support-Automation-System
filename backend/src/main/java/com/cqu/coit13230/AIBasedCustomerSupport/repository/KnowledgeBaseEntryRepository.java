package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;

/**
 * Repository interface for managing {@link KnowledgeBaseEntry} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for
 * knowledge base records through Spring Data JPA.
 * </p>
 */
@Repository
public interface KnowledgeBaseEntryRepository
        extends JpaRepository<KnowledgeBaseEntry, Long> {

}