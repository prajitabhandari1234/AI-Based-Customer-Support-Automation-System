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
 * The endpoints allow knowledge base records to be viewed and maintained.
 */
@RestController
@RequestMapping("/api")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping({"/knowledge-base", "/knowledge"})
    public ResponseEntity<List<KnowledgeBaseEntry>> getAllKnowledgeBaseEntries() {
        return ResponseEntity.ok(knowledgeBaseService.getAllKnowledgeBaseEntries());
    }

    @GetMapping("/knowledge-base/{entryId}")
    public ResponseEntity<KnowledgeBaseEntry> getKnowledgeBaseEntryById(
            @PathVariable Long entryId) {

        return ResponseEntity.ok(
                knowledgeBaseService.getKnowledgeBaseEntryById(entryId));
    }

    @PostMapping({"/knowledge-base", "/knowledge"})
    public ResponseEntity<KnowledgeBaseEntry> createKnowledgeBaseEntry(
            @Valid @RequestBody KnowledgeBaseEntryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(knowledgeBaseService.createKnowledgeBaseEntry(request));
    }

    @PutMapping({"/knowledge-base/{entryId}", "/knowledge/{entryId}"})
    public ResponseEntity<KnowledgeBaseEntry> updateKnowledgeBaseEntry(
            @PathVariable Long entryId,
            @Valid @RequestBody KnowledgeBaseEntryRequest request) {

        return ResponseEntity.ok(
                knowledgeBaseService.updateKnowledgeBaseEntry(entryId, request));
    }

    @DeleteMapping({"/knowledge-base/{entryId}", "/knowledge/{entryId}"})
    public ResponseEntity<Void> deleteKnowledgeBaseEntry(@PathVariable Long entryId) {
        knowledgeBaseService.deleteKnowledgeBaseEntry(entryId);
        return ResponseEntity.noContent().build();
    }
}
