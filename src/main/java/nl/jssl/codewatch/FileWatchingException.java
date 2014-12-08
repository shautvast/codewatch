package nl.jssl.codewatch;

import java.io.IOException;

@SuppressWarnings("serial")
public class FileWatchingException extends RuntimeException {

	public FileWatchingException(IOException e) {
		super(e);
	}

}
