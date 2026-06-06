package com.campus.chat.websocket;

import com.campus.chat.service.ChatService;
import com.campus.common.utils.JwtUtil;
import com.campus.model.dto.ChatMessageDTO;
import com.campus.model.vo.ChatMessageVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatServer extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    private static final String USER_ID_ATTRIBUTE = "userId";

    private final Map<Long, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private final JwtUtil jwtUtil;

    private final ChatService chatService;

    public ChatServer(ObjectMapper objectMapper, JwtUtil jwtUtil, ChatService chatService) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = parseUserId(session.getUri());
        if (userId == null) {
            log.warn("WebSocket connection rejected, invalid token: {}", session.getUri());
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("token invalid"));
            return;
        }
        session.getAttributes().put(USER_ID_ATTRIBUTE, userId);
        onlineSessions.put(userId, session);
        log.info("WebSocket connected, userId={}, sessionId={}", userId, session.getId());
        session.sendMessage(new TextMessage("{\"type\":\"CONNECTED\",\"userId\":" + userId + "}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long fromUid = (Long) session.getAttributes().get(USER_ID_ATTRIBUTE);
        ChatMessageDTO chatMessageDTO = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);
        ChatMessageVO savedMessage = chatService.saveMessage(fromUid, chatMessageDTO);
        String payload = objectMapper.writeValueAsString(savedMessage);

        session.sendMessage(new TextMessage(payload));
        WebSocketSession receiverSession = onlineSessions.get(savedMessage.getToUid());
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(payload));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get(USER_ID_ATTRIBUTE);
        if (userId != null) {
            onlineSessions.remove(userId, session);
            log.info("WebSocket closed, userId={}, sessionId={}, status={}", userId, session.getId(), status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = (Long) session.getAttributes().get(USER_ID_ATTRIBUTE);
        if (userId != null) {
            onlineSessions.remove(userId, session);
        }
        log.warn("WebSocket transport error, userId={}, sessionId={}", userId, session.getId(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private Long parseUserId(URI uri) {
        if (uri == null) {
            return null;
        }
        String token = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("token");
        return jwtUtil.parseUserId(token);
    }
}
