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
     * @param knowledgeBaseEntryService service used to manage knowledge base entries
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
     * @return the requested entry, or HTTP 404 if not found
     */
    @GetMapping("/{entryId}")
    public ResponseEntity<KnowledgeBaseEntry> getKnowledgeBaseEntryById(
            @PathVariable Long entryId) {

        return knowledgeBaseEntryService.getKnowledgeBaseEntryById(entryId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new knowledge base entry.
     *
     * @param entry the knowledge base entry to create
     * @return the created knowledge base entry
     */
    @PostMapping
    public KnowledgeBaseEntry createKnowledgeBaseEntry(
            @RequestBody KnowledgeBaseEntry entry) {

        return knowledgeBaseEntryService.saveKnowledgeBaseEntry(entry);
    }

    /**
     * Updates an existing knowledge base entry.
     *
     * @param entryId the identifier of the knowledge base entry to update
     * @param entry the updated knowledge base entry information
     * @return the updated entry, or HTTP 404 if not found
     */
    @PutMapping("/{entryId}")
    public ResponseEntity<KnowledgeBaseEntry> updateKnowledgeBaseEntry(
            @PathVariable Long entryId,
            @RequestBody KnowledgeBaseEntry entry) {

        return knowledgeBaseEntryService.getKnowledgeBaseEntryById(entryId)
                .map(existingEntry -> {
                    entry.setKbId(entryId);
                    return ResponseEntity.ok(
                            knowledgeBaseEntryService.saveKnowledgeBaseEntry(entry));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a knowledge base entry by identifier.
     *
     * @param entryId the identifier of the knowledge base entry to delete
     * @return HTTP 204 if deleted, or HTTP 404 if not found
     */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteKnowledgeBaseEntry(
            @PathVariable Long entryId) {

        if (knowledgeBaseEntryService
                .getKnowledgeBaseEntryById(entryId)
                .isEmpty()) {

            return ResponseEntity.notFound().build();
        }

        knowledgeBaseEntryService.deleteKnowledgeBaseEntry(entryId);
        return ResponseEntity.noContent().build();
    }
}