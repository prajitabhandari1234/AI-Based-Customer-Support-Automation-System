package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.KnowledgeBaseEntryRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link KnowledgeBaseEntry} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting knowledge base entries.
 * </p>
 */
@Service
public class KnowledgeBaseEntryService {

    private final KnowledgeBaseEntryRepository knowledgeBaseEntryRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code KnowledgeBaseEntryService} with the required
     * repository dependencies.
     *
     * @param knowledgeBaseEntryRepository repository used to access
     *                                     knowledge base data
     * @param userRepository repository used to access user data
     */
    public KnowledgeBaseEntryService(
            KnowledgeBaseEntryRepository knowledgeBaseEntryRepository,
            UserRepository userRepository) {

        this.knowledgeBaseEntryRepository = knowledgeBaseEntryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates or updates a knowledge base entry.
     *
     * <p>
     * Verifies that the user referenced by {@code lastUpdatedBy}
     * exists before the knowledge base entry is saved.
     * </p>
     *
     * @param entry the knowledge base entry to be saved
     * @return the saved knowledge base entry
     * @throws ResourceNotFoundException if the referenced user does not exist
     */
    public KnowledgeBaseEntry saveKnowledgeBaseEntry(
            KnowledgeBaseEntry entry) {

        Long userId = entry.getLastUpdatedBy().getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + userId));

        entry.setLastUpdatedBy(user);

        return knowledgeBaseEntryRepository.save(entry);
    }

    /**
     * Retrieves all knowledge base entries.
     *
     * @return a list of all knowledge base entries
     */
    public List<KnowledgeBaseEntry> getAllKnowledgeBaseEntries() {
        return knowledgeBaseEntryRepository.findAll();
    }

    /**
     * Retrieves a knowledge base entry by its unique identifier.
     *
     * @param entryId the unique identifier of the knowledge base entry
     * @return the knowledge base entry associated with the specified identifier
     * @throws ResourceNotFoundException if no knowledge base entry exists
     *         with the specified identifier
     */
    public KnowledgeBaseEntry getKnowledgeBaseEntryById(Long entryId) {

        return knowledgeBaseEntryRepository.findById(entryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Knowledge base entry not found with ID: "
                                        + entryId));
    }

    /**
     * Deletes a knowledge base entry by identifier.
     *
     * @param entryId the identifier of the knowledge base entry to delete
     */
    public void deleteKnowledgeBaseEntry(Long entryId) {
        knowledgeBaseEntryRepository.deleteById(entryId);
    }
}