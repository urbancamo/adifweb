package uk.m0nom.adifweb.controller;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressFeedbackHandler extends TextWebSocketHandler implements HandshakeInterceptor {

    Map<String, WebSocketSession> webSocketSessions = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // get the JSESSION (HttpSession Id)
        String httpSessionId = "";
        List<String> cookies = session.getHandshakeHeaders().get("cookie");
        for (String cookie : cookies) {
            if (cookie.startsWith("JSESSIONID=")) {
                httpSessionId = cookie.substring(cookie.indexOf("=")+1);
            }
        }
        webSocketSessions.put(httpSessionId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        webSocketSessions.remove(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        /* Shouldn't receive message from the client, so discard */
    }

    public void sendProgressUpdate(String sessionId, String progressMessage) throws IOException {
        if (sessionId != null) {
            WebSocketMessage<String> messageToSend = new TextMessage(progressMessage);
            WebSocketSession webSocketSession = webSocketSessions.get(sessionId);
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.sendMessage(messageToSend);
            }
        }
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}