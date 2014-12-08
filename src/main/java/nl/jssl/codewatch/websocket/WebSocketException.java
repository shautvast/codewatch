package nl.jssl.codewatch.websocket;

import java.io.IOException;

@SuppressWarnings("serial")
public class WebSocketException extends RuntimeException {

	public WebSocketException(String message) {
		super(message);
	}

	public WebSocketException(IOException e) {
		super(e);
	}

}
