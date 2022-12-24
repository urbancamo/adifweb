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
import java.util.logging.Logger;

public class ProgressFeedbackHandler extends TextWebSocketHandler implements HandshakeInterceptor {
    private static final Logger logger = Logger.getLogger(ProgressFeedbackHandler.class.getName());

    Map<String, WebSocketSession> webSocketSessions = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info(String.format("ProgressFeedbackHandler.afterConnectionEstablished called, session=%s", session.getId()));
        super.afterConnectionEstablished(session);
        // get the JSESSION (HttpSession Id)
        String httpSessionId = "";
        List<String> cookies = session.getHandshakeHeaders().get("cookie");
        for (String cookie : cookies) {
            if (cookie.startsWith("JSESSIONID=")) {
                httpSessionId = cookie.substring(cookie.indexOf("=")+1);
                logger.info(String.format("Identified http session id as %s", httpSessionId));
            }
        }
        if (httpSessionId == "") {
            logger.info("Could not identify http session id");
        }
        webSocketSessions.put(httpSessionId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info(String.format("ProgressFeedbackHandler.afterConnectionClosed , session=%s", session.getId()));
        super.afterConnectionClosed(session, status);
        webSocketSessions.remove(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        logger.info("ProgressFeedbackHandler.handleMessage called");
        /* Shouldn't receive message from the client, so discard */
    }

    public void sendProgressUpdate(String sessionId, String progressMessage) throws IOException {

        if (sessionId != null) {
            logger.info(String.format("ProgressFeedbackHandler.sendProgressUpdate httpSessionId=%s, progressMessage=%s", sessionId, progressMessage));
            WebSocketMessage<String> messageToSend = new TextMessage(progressMessage);
            WebSocketSession webSocketSession = webSocketSessions.get(sessionId);
            if (webSocketSession != null && webSocketSession.isOpen()) {
                logger.info("sending message to open web socket session");
                webSocketSession.sendMessage(messageToSend);
            }
        } else {
            logger.info(String.format("ProgressFeedbackHandler.sendProgressUpdate sessionId=null, progressMessage=%s", sessionId, progressMessage));
        }
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        logger.info("ProgressFeedbackHandler.beforeHandshake called");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

        logger.info("ProgressFeedbackHandler.afterHandshake called");
    }
}