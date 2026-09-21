package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.KnowledgeBaseEntryRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.service.KnowledgeBaseService;

import jakarta.validation.Valid;

/**
 * Handles knowledge base management endpoints.
 *
 * <p>
 * This controller provides REST API operations for viewing, creating,
 * updating, and deleting knowledge base records used by the customer
 * support system.
 * </p>
 *
 * <p>
 * Knowledge base operations are delegated to the
 * {@link KnowledgeBaseService}, which manages the associated business
 * logic and data operations.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * Creates the knowledge base controller with the required
     * knowledge base service.
     *
     * @param knowledgeBaseService service responsible for managing
     *                             knowledge base entries
     */
    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {

        this.knowledgeBaseService = knowledgeBaseService;

    }

    /**
     * Retrieves all knowledge base entries available in the system.
     *
     * <p>
     * The endpoint supports both {@code /knowledge-base} and
     * {@code /knowledge} URL paths for retrieving the available
     * knowledge base records.
     * </p>
     *
     * @return a response containing the list of all knowledge base entries
     */
    @GetMapping({ "/knowledge-base", "/knowledge" })
    public ResponseEntity<List<KnowledgeBaseEntry>> getAllKnowledgeBaseEntries() {

        return ResponseEntity.ok(knowledgeBaseService.getAllKnowledgeBaseEntries());

    }

    /**
     * Retrieves a specific knowledge base entry using its unique identifier.
     *
     * @param entryId unique identifier of the knowledge base entry to retrieve
     * @return a response containing the requested knowledge base entry
     */
    @GetMapping("/knowledge-base/{entryId}")
    public ResponseEntity<KnowledgeBaseEntry> getKnowledgeBaseEntryById(

            @PathVariable Long entryId) {

        return ResponseEntity.ok(

                knowledgeBaseService.getKnowledgeBaseEntryById(entryId));

    }

    /**
     * Creates a new knowledge base entry.
     *
     * <p>
     * The supplied request is validated before being passed to the
     * knowledge base service for creation. The endpoint supports both
     * {@code /knowledge-base} and {@code /knowledge} URL paths.
     * </p>
     *
     * <p>
     * A successful operation returns an HTTP 201 Created response
     * containing the newly created knowledge base entry.
     * </p>
     *
     * @param request validated request containing the information required
     *                to create the knowledge base entry
     * @return a response containing the newly created knowledge base entry
     */
    @PostMapping({ "/knowledge-base", "/knowledge" })
    public ResponseEntity<KnowledgeBaseEntry> createKnowledgeBaseEntry(

            @Valid @RequestBody KnowledgeBaseEntryRequest request) {

        return ResponseEntity

                .status(HttpStatus.CREATED)

                .body(knowledgeBaseService.createKnowledgeBaseEntry(request));

    }

    /**
     * Updates an existing knowledge base entry.
     *
     * <p>
     * The entry identifier specifies the knowledge base record to update,
     * while the validated request contains the updated information.
     * The endpoint supports both {@code /knowledge-base/{entryId}} and
     * {@code /knowledge/{entryId}} URL paths.
     * </p>
     *
     * @param entryId unique identifier of the knowledge base entry to update
     * @param request validated request containing the updated knowledge
     *                base information
     * @return a response containing the updated knowledge base entry
     */
    @PutMapping({ "/knowledge-base/{entryId}", "/knowledge/{entryId}" })
    public ResponseEntity<KnowledgeBaseEntry> updateKnowledgeBaseEntry(

            @PathVariable Long entryId,

            @Valid @RequestBody KnowledgeBaseEntryRequest request) {

        return ResponseEntity.ok(

                knowledgeBaseService.updateKnowledgeBaseEntry(entryId, request));

    }

    /**
     * Deletes an existing knowledge base entry using its unique identifier.
     *
     * <p>
     * The endpoint supports both {@code /knowledge-base/{entryId}} and
     * {@code /knowledge/{entryId}} URL paths. After successful deletion,
     * the endpoint returns an HTTP 204 No Content response.
     * </p>
     *
     * @param entryId unique identifier of the knowledge base entry to delete
     * @return an empty response indicating successful deletion
     */
    @DeleteMapping({ "/knowledge-base/{entryId}", "/knowledge/{entryId}" })
    public ResponseEntity<Void> deleteKnowledgeBaseEntry(@PathVariable Long entryId) {

        knowledgeBaseService.deleteKnowledgeBaseEntry(entryId);

        return ResponseEntity.noContent().build();

    }

}