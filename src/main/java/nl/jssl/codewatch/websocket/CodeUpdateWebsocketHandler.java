package nl.jssl.codewatch.websocket;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class CodeUpdateWebsocketHandler extends TextWebSocketHandler {
	private final static Logger log = LoggerFactory.getLogger(CodeUpdateWebsocketHandler.class);
	private WebSocketSession session;

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		log.info("new connection from {} ", session.getRemoteAddress());
		try {
			if (this.session != null) {
				this.session.close();
			}
		} catch (IOException e) {
			throw new WebSocketException(e);
		}
		this.session = session;
	}

	public void send(String message) {
		if (session != null) {
			try {
				session.sendMessage(new TextMessage(message.getBytes()));
			} catch (IOException e) {
				throw new WebSocketException(e);
			}
		}
	}
}