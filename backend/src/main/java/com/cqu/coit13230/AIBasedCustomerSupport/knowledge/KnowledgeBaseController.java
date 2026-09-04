package com.cqu.coit13230.AIBasedCustomerSupport.knowledge;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {
    private final KnowledgeBaseService service;

    public KnowledgeBaseController(KnowledgeBaseService service) {
        this.service = service;
    }

    @GetMapping
    public List<KnowledgeBaseView> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeBaseView create(@Valid @RequestBody KnowledgeBaseRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public KnowledgeBaseView update(@PathVariable Long id, @Valid @RequestBody KnowledgeBaseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
