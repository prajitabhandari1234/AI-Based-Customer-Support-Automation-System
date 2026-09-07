package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import com.cqu.coit13230.AIBasedCustomerSupport.service.KnowledgeBaseEntryService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link KnowledgeBaseEntry} entities.
 *
 * <p>
 * All authenticated users may retrieve knowledge base entries.
 * Modification operations are restricted to administrators and
 * support agents through the application's security configuration
 * and service-layer authorization checks.
 * </p>
 *
 * <p>
 * For creation, update, and deletion operations, the authenticated
 * user's identity is obtained from JWT authentication rather than
 * being supplied directly by the client.
 * </p>
 */
@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseEntryController {

    /**
     * Service used to manage knowledge base entries.
     */
    private final KnowledgeBaseEntryService knowledgeBaseEntryService;

    /**
     * Constructs a new {@code KnowledgeBaseEntryController}.
     *
     * @param knowledgeBaseEntryService service used to manage
     *        knowledge base entries
     */
    public KnowledgeBaseEntryController(
            KnowledgeBaseEntryService knowledgeBaseEntryService) {

        this.knowledgeBaseEntryService =
                knowledgeBaseEntryService;
    }

    /**
     * Retrieves all knowledge base entries.
     *
     * @return list containing all knowledge base entries
     */
    @GetMapping
    public ResponseEntity<List<KnowledgeBaseEntry>>
            getAllKnowledgeBaseEntries() {

        List<KnowledgeBaseEntry> entries =
                knowledgeBaseEntryService
                        .getAllKnowledgeBaseEntries();

        return ResponseEntity.ok(entries);
    }

    /**
     * Retrieves a knowledge base entry by identifier.
     *
     * @param entryId identifier of the knowledge base entry
     * @return requested knowledge base entry
     */
    @GetMapping("/{entryId}")
    public ResponseEntity<KnowledgeBaseEntry>
            getKnowledgeBaseEntryById(
                    @PathVariable Long entryId) {

        KnowledgeBaseEntry entry =
                knowledgeBaseEntryService
                        .getKnowledgeBaseEntryById(entryId);

        return ResponseEntity.ok(entry);
    }

    /**
     * Creates a new knowledge base entry.
     *
     * <p>
     * The user responsible for creating the entry is determined from
     * JWT authentication and is not accepted from the request body.
     * </p>
     *
     * @param request knowledge base information
     * @param authentication authenticated user information
     * @return newly created knowledge base entry
     */
    @PostMapping
    public ResponseEntity<KnowledgeBaseEntry>
            createKnowledgeBaseEntry(
                    @Valid
                    @RequestBody KnowledgeBaseEntryRequest request,
                    Authentication authentication) {

        KnowledgeBaseEntry createdEntry =
                knowledgeBaseEntryService
                        .createKnowledgeBaseEntry(
                                request,
                                authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdEntry);
    }

    /**
     * Updates an existing knowledge base entry.
     *
     * <p>
     * The authenticated user automatically becomes the
     * {@code lastUpdatedBy} user.
     * </p>
     *
     * @param entryId identifier of the knowledge base entry
     * @param request updated knowledge base information
     * @param authentication authenticated user information
     * @return updated knowledge base entry
     */
    @PutMapping("/{entryId}")
    public ResponseEntity<KnowledgeBaseEntry>
            updateKnowledgeBaseEntry(
                    @PathVariable Long entryId,
                    @Valid
                    @RequestBody KnowledgeBaseEntryRequest request,
                    Authentication authentication) {

        KnowledgeBaseEntry updatedEntry =
                knowledgeBaseEntryService
                        .updateKnowledgeBaseEntry(
                                entryId,
                                request,
                                authentication.getName());

        return ResponseEntity.ok(updatedEntry);
    }

    /**
     * Deletes an existing knowledge base entry.
     *
     * @param entryId identifier of the knowledge base entry
     * @param authentication authenticated user information
     * @return HTTP 204 after successful deletion
     */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteKnowledgeBaseEntry(
            @PathVariable Long entryId,
            Authentication authentication) {

        knowledgeBaseEntryService
                .deleteKnowledgeBaseEntry(
                        entryId,
                        authentication.getName());

        return ResponseEntity
                .noContent()
                .build();
    }
}