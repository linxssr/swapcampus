package com.campus.chat.config;

import com.campus.chat.websocket.ChatServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final ChatServer chatServer;

    public ChatWebSocketConfig(ChatServer chatServer) {
        this.chatServer = chatServer;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatServer, "/ws/chat")
                .setAllowedOriginPatterns("*");
    }
}
