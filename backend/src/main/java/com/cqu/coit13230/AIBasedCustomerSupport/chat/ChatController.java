package com.cqu.coit13230.AIBasedCustomerSupport.chat;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @PostMapping("/messages")
    public ChatResponse send(@Valid @RequestBody ChatRequest request) {
        return service.send(request);
    }
}
