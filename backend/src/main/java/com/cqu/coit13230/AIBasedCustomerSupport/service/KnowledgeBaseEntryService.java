package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.KnowledgeBaseEntryRepository;

/**
 * Service class responsible for managing {@link KnowledgeBaseEntry} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting knowledge base entries through the
 * {@link KnowledgeBaseEntryRepository}.
 * </p>
 */
@Service
public class KnowledgeBaseEntryService {

    private final KnowledgeBaseEntryRepository knowledgeBaseEntryRepository;

    /**
     * Constructs a new {@code KnowledgeBaseEntryService} with the required
     * knowledge base repository dependency.
     *
     * @param knowledgeBaseEntryRepository repository used to access
     *                                     knowledge base data
     */
    public KnowledgeBaseEntryService(
            KnowledgeBaseEntryRepository knowledgeBaseEntryRepository) {
        this.knowledgeBaseEntryRepository = knowledgeBaseEntryRepository;
    }

    /**
     * Creates or updates a knowledge base entry.
     *
     * @param entry the knowledge base entry to be saved
     * @return the saved knowledge base entry
     */
    public KnowledgeBaseEntry saveKnowledgeBaseEntry(KnowledgeBaseEntry entry) {
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
     * Retrieves a knowledge base entry by identifier.
     *
     * @param entryId the identifier of the knowledge base entry
     * @return an optional containing the entry if found
     */
    public Optional<KnowledgeBaseEntry> getKnowledgeBaseEntryById(Long entryId) {
        return knowledgeBaseEntryRepository.findById(entryId);
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