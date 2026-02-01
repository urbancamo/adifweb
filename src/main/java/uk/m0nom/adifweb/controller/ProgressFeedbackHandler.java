package uk.m0nom.adifweb.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

// WebSocketSession lifecycle is managed by Spring, not by this handler
@SuppressWarnings("resource")
public class ProgressFeedbackHandler extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger(ProgressFeedbackHandler.class.getName());

    Map<String, WebSocketSession> webSocketSessions = Collections.synchronizedMap(new HashMap<>());
    Map<WebSocketSession, String> webSocketSessionsReverseMap = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        logger.info("ProgressFeedbackHandler.afterConnectionEstablished called");
        super.afterConnectionEstablished(session);

        String httpSessionId = null;

        // First, try to get session ID from URL query parameter (most reliable cross-browser)
        if (session.getUri() != null) {
            String query = session.getUri().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("sessionId=")) {
                        httpSessionId = URLDecoder.decode(param.substring("sessionId=".length()), StandardCharsets.UTF_8);
                        logger.info(String.format("Got sessionId from URL parameter: '%s'", httpSessionId));
                        break;
                    }
                }
            }
        }

        // Fallback: try to get JSESSIONID from cookie (may not work in Safari/strict privacy browsers)
        if (httpSessionId == null) {
            List<String> cookies = session.getHandshakeHeaders().get("cookie");
            if (cookies != null) {
                for (String cookie : cookies) {
                    StringTokenizer tokenizer = new StringTokenizer(cookie, ";");
                    while (tokenizer.hasMoreTokens()) {
                        String keyValuePair = tokenizer.nextToken().trim();
                        if (keyValuePair.startsWith("JSESSIONID=")) {
                            httpSessionId = keyValuePair.substring(keyValuePair.indexOf("=") + 1);
                            logger.info(String.format("Got httpSessionId from cookie: '%s'", httpSessionId));
                            break;
                        }
                    }
                }
            }
        }

        if (httpSessionId == null) {
            httpSessionId = "empty";
            logger.warning("Could not identify httpSessionId from URL or cookie, storing under 'empty'");
        }

        logger.info(String.format("Mapping httpSessionId='%s' to webSocket sessionId='%s'", httpSessionId, session.getId()));
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
            logger.info(String.format("sendProgressUpdate called: sessionId='%s', message='%s', knownSessions=%s",
                    sessionId, progressMessage, webSocketSessions.keySet()));
            WebSocketMessage<String> messageToSend = new TextMessage(progressMessage);
            WebSocketSession webSocketSession = webSocketSessions.get(sessionId);
            if (webSocketSession != null && webSocketSession.isOpen()) {
                logger.info(String.format("Sending message to open WebSocket session: wsSessionId='%s'", webSocketSession.getId()));
                try {
                    webSocketSession.sendMessage(messageToSend);
                } catch (IOException e) {
                    logger.warning(String.format("Caught exception %s sending message to httpSessionId='%s', webSocketSessionId='%s'", e.getMessage(), sessionId, webSocketSession.getId()));
                }
            } else {
                logger.warning(String.format("No open WebSocket session found for httpSessionId='%s'", sessionId));
            }
        } else {
            logger.warning(String.format("sendProgressUpdate called with sessionId=null, progressMessage='%s'", progressMessage));
        }
    }
}