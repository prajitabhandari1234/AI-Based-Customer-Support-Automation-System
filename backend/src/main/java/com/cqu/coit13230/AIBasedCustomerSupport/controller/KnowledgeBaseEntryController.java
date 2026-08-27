package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.service.KnowledgeBaseEntryService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link KnowledgeBaseEntry} entities.
 *
 * <p>
 * Provides API endpoints for creating, retrieving, updating,
 * and deleting knowledge base entries through the
 * {@link KnowledgeBaseEntryService}.
 * </p>
 */
@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseEntryController {

    private final KnowledgeBaseEntryService knowledgeBaseEntryService;

    /**
     * Constructs a new {@code KnowledgeBaseEntryController} with the required
     * knowledge base service.
     *
     * @param knowledgeBaseEntryService service used to manage
     *                                  knowledge base entries
     */
    public KnowledgeBaseEntryController(
            KnowledgeBaseEntryService knowledgeBaseEntryService) {

        this.knowledgeBaseEntryService = knowledgeBaseEntryService;
    }

    /**
     * Retrieves all knowledge base entries.
     *
     * @return a list of all knowledge base entries
     */
    @GetMapping
    public List<KnowledgeBaseEntry> getAllKnowledgeBaseEntries() {
        return knowledgeBaseEntryService.getAllKnowledgeBaseEntries();
    }

    /**
     * Retrieves a knowledge base entry by identifier.
     *
     * @param entryId the identifier of the knowledge base entry
     * @return the requested knowledge base entry
     */
    @GetMapping("/{entryId}")
    public ResponseEntity<KnowledgeBaseEntry> getKnowledgeBaseEntryById(
            @PathVariable Long entryId) {

        return ResponseEntity.ok(
                knowledgeBaseEntryService.getKnowledgeBaseEntryById(entryId));
    }

    /**
     * Creates a new knowledge base entry.
     *
     * @param entry the knowledge base entry to create
     * @return the created knowledge base entry
     */
    @PostMapping
    public KnowledgeBaseEntry createKnowledgeBaseEntry(
            @Valid @RequestBody KnowledgeBaseEntry entry) {

        return knowledgeBaseEntryService.saveKnowledgeBaseEntry(entry);
    }

    /**
     * Updates an existing knowledge base entry.
     *
     * @param entryId the identifier of the knowledge base entry to update
     * @param entry the updated knowledge base entry information
     * @return the updated knowledge base entry
     */
    @PutMapping("/{entryId}")
    public ResponseEntity<KnowledgeBaseEntry> updateKnowledgeBaseEntry(
            @PathVariable Long entryId,
            @Valid @RequestBody KnowledgeBaseEntry entry) {

        knowledgeBaseEntryService.getKnowledgeBaseEntryById(entryId);

        entry.setKbId(entryId);

        return ResponseEntity.ok(
                knowledgeBaseEntryService.saveKnowledgeBaseEntry(entry));
    }

    /**
     * Deletes a knowledge base entry by identifier.
     *
     * @param entryId the identifier of the knowledge base entry to delete
     * @return HTTP 204 after successful deletion
     */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteKnowledgeBaseEntry(
            @PathVariable Long entryId) {

        knowledgeBaseEntryService.getKnowledgeBaseEntryById(entryId);

        knowledgeBaseEntryService.deleteKnowledgeBaseEntry(entryId);

        return ResponseEntity.noContent().build();
    }
}