package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.KnowledgeBaseEntryRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.KnowledgeBaseEntryRepository;

/**
 * Provides business logic and persistence operations for knowledge base
 * entries used by the AI-based customer support system.
 *
 * <p>
 * This service manages the retrieval, creation, updating, and deletion
 * of knowledge base entries. These entries provide maintained support
 * information that can be used by the system when generating customer
 * support responses.
 * </p>
 */
@Service
public class KnowledgeBaseService {

    /**
     * Repository used to perform persistence and retrieval operations
     * for knowledge base entries.
     */
    private final KnowledgeBaseEntryRepository knowledgeBaseRepository;

    /**
     * Service used to obtain information about the currently
     * authenticated user.
     */
    private final UserService userService;

    /**
     * Creates a knowledge base service with the required repository
     * and user service dependencies.
     *
     * @param knowledgeBaseRepository repository used to access
     *                                knowledge base entries
     * @param userService             service used to access the currently
     *                                authenticated user
     */
    public KnowledgeBaseService(
            KnowledgeBaseEntryRepository knowledgeBaseRepository,
            UserService userService) {

        this.knowledgeBaseRepository = knowledgeBaseRepository;

        this.userService = userService;

    }

    /**
     * Retrieves all knowledge base entries ordered by category and
     * question pattern in ascending order.
     *
     * @return list containing all knowledge base entries
     */
    public List<KnowledgeBaseEntry> getAllKnowledgeBaseEntries() {

        return knowledgeBaseRepository.findAllByOrderByCategoryAscQuestionPatternAsc();

    }

    /**
     * Retrieves all active knowledge base entries ordered by category
     * and question pattern in ascending order.
     *
     * @return list containing all active knowledge base entries
     */
    public List<KnowledgeBaseEntry> getActiveKnowledgeBaseEntries() {

        return knowledgeBaseRepository.findByActiveTrueOrderByCategoryAscQuestionPatternAsc();

    }

    /**
     * Retrieves a knowledge base entry using its unique identifier.
     *
     * @param entryId unique identifier of the knowledge base entry
     * @return knowledge base entry matching the supplied identifier
     * @throws ResourceNotFoundException if no knowledge base entry exists
     *                                   with the supplied identifier
     */
    public KnowledgeBaseEntry getKnowledgeBaseEntryById(Long entryId) {

        return knowledgeBaseRepository.findById(entryId)

                .orElseThrow(() -> new ResourceNotFoundException(

                        "Knowledge base entry not found with ID: " + entryId));

    }

    /**
     * Creates and persists a new knowledge base entry using the
     * supplied request information.
     *
     * <p>
     * The currently authenticated user is recorded as the user who
     * last updated the knowledge base entry.
     * </p>
     *
     * @param request request containing the knowledge base entry information
     * @return newly created and persisted knowledge base entry
     */
    public KnowledgeBaseEntry createKnowledgeBaseEntry(KnowledgeBaseEntryRequest request) {

        KnowledgeBaseEntry entry = new KnowledgeBaseEntry();

        apply(entry, request, userService.currentUser());

        return knowledgeBaseRepository.save(entry);

    }

    /**
     * Updates an existing knowledge base entry using the supplied
     * request information.
     *
     * <p>
     * The existing entry is retrieved before the supplied values are
     * applied, and the currently authenticated user is recorded as
     * the user who last updated the entry.
     * </p>
     *
     * @param entryId unique identifier of the knowledge base entry to update
     * @param request request containing the updated knowledge base information
     * @return updated and persisted knowledge base entry
     * @throws ResourceNotFoundException if no knowledge base entry exists
     *                                   with the supplied identifier
     */
    public KnowledgeBaseEntry updateKnowledgeBaseEntry(

            Long entryId,

            KnowledgeBaseEntryRequest request) {

        KnowledgeBaseEntry entry = getKnowledgeBaseEntryById(entryId);

        apply(entry, request, userService.currentUser());

        return knowledgeBaseRepository.save(entry);

    }

    /**
     * Deletes a knowledge base entry using its unique identifier.
     *
     * <p>
     * The entry is retrieved first to verify that it exists before
     * the repository deletion operation is performed.
     * </p>
     *
     * @param entryId unique identifier of the knowledge base entry to delete
     * @throws ResourceNotFoundException if no knowledge base entry exists
     *                                   with the supplied identifier
     */
    public void deleteKnowledgeBaseEntry(Long entryId) {

        getKnowledgeBaseEntryById(entryId);

        knowledgeBaseRepository.deleteById(entryId);

    }

    /**
     * Applies request values to a knowledge base entry and records the
     * user responsible for the update.
     *
     * <p>
     * The question pattern and answer template are trimmed, the category
     * is trimmed and converted to uppercase, and the active status defaults
     * to {@code true} when no value is supplied.
     * </p>
     *
     * @param entry   knowledge base entry receiving the values
     * @param request request containing the values to apply
     * @param user    user responsible for creating or updating the entry
     */
    private void apply(

            KnowledgeBaseEntry entry,

            KnowledgeBaseEntryRequest request,

            User user) {

        entry.setQuestionPattern(request.getQuestionPattern().trim());

        entry.setAnswerTemplate(request.getAnswerTemplate().trim());

        entry.setCategory(request.getCategory().trim().toUpperCase());

        entry.setActive(request.getActive() == null ? true : request.getActive());

        entry.setLastUpdatedBy(user);

    }

}