package com.cqu.coit13230.AIBasedCustomerSupport.chat;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketService;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final TicketService tickets;

    public ChatService(TicketService tickets) {
        this.tickets = tickets;
    }

    public ChatResponse send(ChatRequest request) {
        return request.ticketId() == null
                ? tickets.startChat(request.message())
                : tickets.continueChat(request.ticketId(), request.message());
    }
}
