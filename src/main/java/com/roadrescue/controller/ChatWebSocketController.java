package com.roadrescue.controller;

import com.roadrescue.model.ChatMessage;
import com.roadrescue.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatWebSocketController(ChatMessageRepository chatMessageRepository,
                                    SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{requestId}")
    public void handleChat(@DestinationVariable Long requestId,
                            ChatMessage incoming,
                            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        Object uid = sessionAttributes.get("userId");
        Object uname = sessionAttributes.get("userName");

        // Not authenticated on this WebSocket session (e.g. session expired) -> drop silently.
        if (uid == null) {
            return;
        }

        ChatMessage message = new ChatMessage();
        message.setRequestId(requestId);
        message.setSenderId(((Number) uid).longValue());
        message.setSenderName(String.valueOf(uname));
        message.setContent(incoming.getContent());

        ChatMessage saved = chatMessageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/chat/" + requestId, saved);
    }
}
