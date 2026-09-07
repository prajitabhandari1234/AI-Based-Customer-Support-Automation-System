package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.KnowledgeBaseEntryRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.KnowledgeBaseEntryRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link KnowledgeBaseEntry} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting knowledge base entries.
 * </p>
 *
 * <p>
 * Knowledge base modification operations identify the responsible
 * administrator or support agent from JWT authentication instead of
 * trusting user information supplied by the client.
 * </p>
 */
@Service
public class KnowledgeBaseEntryService {

    /**
     * Repository used to access knowledge base entries.
     */
    private final KnowledgeBaseEntryRepository knowledgeBaseEntryRepository;

    /**
     * Repository used to access application users.
     */
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code KnowledgeBaseEntryService}.
     *
     * @param knowledgeBaseEntryRepository repository used to access
     *        knowledge base data
     * @param userRepository repository used to access user data
     */
    public KnowledgeBaseEntryService(
            KnowledgeBaseEntryRepository knowledgeBaseEntryRepository,
            UserRepository userRepository) {

        this.knowledgeBaseEntryRepository =
                knowledgeBaseEntryRepository;

        this.userRepository = userRepository;
    }

    /**
     * Creates a new knowledge base entry.
     *
     * <p>
     * The authenticated user's identity is obtained from the JWT
     * principal. Only administrators and support agents are permitted
     * to create knowledge base entries.
     * </p>
     *
     * @param request knowledge base information supplied by the client
     * @param authenticatedEmail email obtained from JWT authentication
     * @return newly created knowledge base entry
     * @throws ResourceNotFoundException if the authenticated user cannot
     *         be found
     * @throws ForbiddenOperationException if the authenticated user does
     *         not have permission to maintain the knowledge base
     */
    public KnowledgeBaseEntry createKnowledgeBaseEntry(
            KnowledgeBaseEntryRequest request,
            String authenticatedEmail) {

        User authenticatedUser =
                getAuthorizedKnowledgeBaseUser(
                        authenticatedEmail);

        KnowledgeBaseEntry entry =
                new KnowledgeBaseEntry();

        entry.setQuestionPattern(
                request.getQuestionPattern().trim());

        entry.setAnswerTemplate(
                request.getAnswerTemplate().trim());

        entry.setCategory(
                request.getCategory().trim());

        entry.setLastUpdatedBy(authenticatedUser);

        return knowledgeBaseEntryRepository.save(entry);
    }

    /**
     * Updates an existing knowledge base entry.
     *
     * <p>
     * The identity of the user performing the update is obtained from
     * JWT authentication and stored automatically as the
     * {@code lastUpdatedBy} user.
     * </p>
     *
     * @param entryId unique identifier of the knowledge base entry
     * @param request updated knowledge base information
     * @param authenticatedEmail email obtained from JWT authentication
     * @return updated knowledge base entry
     * @throws ResourceNotFoundException if the entry or authenticated
     *         user cannot be found
     * @throws ForbiddenOperationException if the authenticated user does
     *         not have permission to maintain the knowledge base
     */
    public KnowledgeBaseEntry updateKnowledgeBaseEntry(
            Long entryId,
            KnowledgeBaseEntryRequest request,
            String authenticatedEmail) {

        KnowledgeBaseEntry existingEntry =
                getKnowledgeBaseEntryById(entryId);

        User authenticatedUser =
                getAuthorizedKnowledgeBaseUser(
                        authenticatedEmail);

        existingEntry.setQuestionPattern(
                request.getQuestionPattern().trim());

        existingEntry.setAnswerTemplate(
                request.getAnswerTemplate().trim());

        existingEntry.setCategory(
                request.getCategory().trim());

        existingEntry.setLastUpdatedBy(authenticatedUser);

        return knowledgeBaseEntryRepository.save(existingEntry);
    }

    /**
     * Retrieves all knowledge base entries.
     *
     * @return list containing all knowledge base entries
     */
    public List<KnowledgeBaseEntry> getAllKnowledgeBaseEntries() {

        return knowledgeBaseEntryRepository.findAll();
    }

    /**
     * Retrieves a knowledge base entry by its identifier.
     *
     * @param entryId unique identifier of the knowledge base entry
     * @return matching knowledge base entry
     * @throws ResourceNotFoundException if the entry cannot be found
     */
    public KnowledgeBaseEntry getKnowledgeBaseEntryById(
            Long entryId) {

        return knowledgeBaseEntryRepository
                .findById(entryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Knowledge base entry not found with ID: "
                                        + entryId));
    }

    /**
     * Deletes a knowledge base entry.
     *
     * <p>
     * The authenticated user is verified before deletion. Only
     * administrators and support agents may delete knowledge base
     * entries.
     * </p>
     *
     * @param entryId unique identifier of the entry to delete
     * @param authenticatedEmail email obtained from JWT authentication
     * @throws ResourceNotFoundException if the entry or authenticated
     *         user cannot be found
     * @throws ForbiddenOperationException if the authenticated user does
     *         not have permission to maintain the knowledge base
     */
    public void deleteKnowledgeBaseEntry(
            Long entryId,
            String authenticatedEmail) {

        getAuthorizedKnowledgeBaseUser(
                authenticatedEmail);

        KnowledgeBaseEntry entry =
                getKnowledgeBaseEntryById(entryId);

        knowledgeBaseEntryRepository.delete(entry);
    }

    /**
     * Retrieves and validates a user permitted to maintain the
     * knowledge base.
     *
     * <p>
     * Only users with the {@link UserRole#SUPPORT_AGENT} or
     * {@link UserRole#ADMIN} role are authorised.
     * </p>
     *
     * @param authenticatedEmail authenticated user's email address
     * @return authorised application user
     * @throws ResourceNotFoundException if the user cannot be found
     * @throws ForbiddenOperationException if the user does not have
     *         an authorised role
     */
    private User getAuthorizedKnowledgeBaseUser(
            String authenticatedEmail) {

        String normalizedEmail =
                authenticatedEmail
                        .trim()
                        .toLowerCase();

        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: "
                                        + normalizedEmail));

        if (user.getRole() != UserRole.SUPPORT_AGENT
                && user.getRole() != UserRole.ADMIN) {

            throw new ForbiddenOperationException(
                    "Only support agents and administrators "
                            + "can manage knowledge base entries");
        }

        return user;
    }
}