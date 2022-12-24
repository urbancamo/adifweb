package uk.m0nom.adifweb.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private WebSocketHandlerRegistry webSocketHandlerRegistry;
    private ProgressFeedbackHandler progressFeedbackHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        this.webSocketHandlerRegistry = webSocketHandlerRegistry;
        this.progressFeedbackHandler = new ProgressFeedbackHandler();
        webSocketHandlerRegistry
                .addHandler(progressFeedbackHandler, "/progress");
                //.addInterceptors(progressFeedbackHandler);
    }

    public WebSocketHandlerRegistry getWebSocketHandlerRegistry() {
        return webSocketHandlerRegistry;
    }

    public ProgressFeedbackHandler getProgressFeedbackHandler() {
        return progressFeedbackHandler;
    }
}