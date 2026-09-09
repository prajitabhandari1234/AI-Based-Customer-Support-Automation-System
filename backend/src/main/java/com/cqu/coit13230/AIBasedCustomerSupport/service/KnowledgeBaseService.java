package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.KnowledgeBaseEntryRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.KnowledgeBaseEntryRepository;

/**
 * Handles knowledge base entries used by the support system.
 * It provides the database operations used to maintain AI support answers.
 */
@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseEntryRepository knowledgeBaseRepository;
    private final UserService userService;

    public KnowledgeBaseService(
            KnowledgeBaseEntryRepository knowledgeBaseRepository,
            UserService userService) {

        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.userService = userService;
    }

    public List<KnowledgeBaseEntry> getAllKnowledgeBaseEntries() {
        return knowledgeBaseRepository.findAllByOrderByCategoryAscQuestionPatternAsc();
    }

    public List<KnowledgeBaseEntry> getActiveKnowledgeBaseEntries() {
        return knowledgeBaseRepository.findByActiveTrueOrderByCategoryAscQuestionPatternAsc();
    }

    public KnowledgeBaseEntry getKnowledgeBaseEntryById(Long entryId) {
        return knowledgeBaseRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Knowledge base entry not found with ID: " + entryId));
    }

    public KnowledgeBaseEntry createKnowledgeBaseEntry(KnowledgeBaseEntryRequest request) {
        KnowledgeBaseEntry entry = new KnowledgeBaseEntry();
        apply(entry, request, userService.currentUser());
        return knowledgeBaseRepository.save(entry);
    }

    public KnowledgeBaseEntry updateKnowledgeBaseEntry(
            Long entryId,
            KnowledgeBaseEntryRequest request) {

        KnowledgeBaseEntry entry = getKnowledgeBaseEntryById(entryId);
        apply(entry, request, userService.currentUser());
        return knowledgeBaseRepository.save(entry);
    }

    public void deleteKnowledgeBaseEntry(Long entryId) {
        getKnowledgeBaseEntryById(entryId);
        knowledgeBaseRepository.deleteById(entryId);
    }

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
