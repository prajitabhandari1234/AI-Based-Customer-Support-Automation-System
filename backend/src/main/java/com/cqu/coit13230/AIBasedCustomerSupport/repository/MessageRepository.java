package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;

/**
 * Repository interface for managing {@link Message} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for
 * conversation message records through Spring Data JPA.
 * </p>
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

}