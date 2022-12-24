package uk.m0nom.adifweb.controller;

import org.jetbrains.annotations.NotNull;
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
import java.util.logging.Logger;

public class ProgressFeedbackHandler extends TextWebSocketHandler implements HandshakeInterceptor {
    private static final Logger logger = Logger.getLogger(ProgressFeedbackHandler.class.getName());

    Map<String, WebSocketSession> webSocketSessions = Collections.synchronizedMap(new HashMap<>());
    Map<WebSocketSession, String> webSocketSessionsReverseMap = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        logger.info("ProgressFeedbackHandler.afterConnectionEstablished called");
        super.afterConnectionEstablished(session);
        // get the JSESSION (HttpSession Id)
        String httpSessionId = "empty";
        List<String> cookies = session.getHandshakeHeaders().get("cookie");
        for (String cookie : cookies) {
            if (cookie.startsWith("JSESSIONID=")) {
                httpSessionId = cookie.substring(cookie.indexOf("=")+1);
                logger.info(String.format("Identified httpSessionId='%s', webSocket sessionId='%s'", httpSessionId, session.getId()));
            }
        }
        if ("empty".equals(httpSessionId)) {
            logger.info("Could not identify httpSessionId, storing under 'empty' httpSessionId");
        }
        webSocketSessions.put(httpSessionId, session);
        webSocketSessionsReverseMap.put(session, httpSessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info(String.format("ProgressFeedbackHandler.afterConnectionClosed called session='%s', status='%s'", session.getId(), status.toString()));
        super.afterConnectionClosed(session, status);
        String httpSessionId = webSocketSessionsReverseMap.get(session);
        webSocketSessionsReverseMap.remove(session);
        webSocketSessions.remove(httpSessionId);
    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) {
        logger.warning("ProgressFeedbackHandler.handleMessage unexpectedly called");
        /* Shouldn't receive message from the client, so discard */
    }

    public void sendProgressUpdate(String sessionId, String progressMessage) {
        if (sessionId != null) {
            logger.info(String.format("ProgressFeedbackHandler.sendProgressUpdate httpSessionId='%s', progressMessage='%s'", sessionId, progressMessage));
            WebSocketMessage<String> messageToSend = new TextMessage(progressMessage);
            WebSocketSession webSocketSession = webSocketSessions.get(sessionId);
            if (webSocketSession != null && webSocketSession.isOpen()) {
                logger.info("sending message to open web socket session");
                try {
                    webSocketSession.sendMessage(messageToSend);
                } catch (IOException e) {
                    logger.warning(String.format("Caught exception %s sending message to httpSessionId='%s', webSocketSessionId='%s'", e.getMessage(), sessionId, webSocketSession.getId()));
                }
            }
        } else {
            logger.info(String.format("ProgressFeedbackHandler.sendProgressUpdate sessionId=null, progressMessage='%s'", progressMessage));
        }
    }

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,
                                   @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {

        logger.info("ProgressFeedbackHandler.beforeHandshake called");
        return false;
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,
                               @NotNull WebSocketHandler wsHandler, Exception exception) {

        logger.info("ProgressFeedbackHandler.afterHandshake called");
    }
}