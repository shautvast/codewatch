package nl.jssl.codewatch;

import java.nio.file.Path;

public interface FileWatchCallBack {
	void handle(Path child);
}
