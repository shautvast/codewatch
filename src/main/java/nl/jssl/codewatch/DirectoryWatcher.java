package nl.jssl.codewatch;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util to watch a directory (or tree) for changes to files.
 */
public class DirectoryWatcher {
	private final static Logger log = LoggerFactory.getLogger(DirectoryWatcher.class);

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;

	public static void watch(String dirname, final FileWatchCallBack callback) throws Exception {
		final Path dir = Paths.get(dirname);
		Executors.newFixedThreadPool(1).submit(new Runnable() {
			@Override
			public void run() {
				new DirectoryWatcher(dir, true).processEvents(callback);
			}
		});
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	DirectoryWatcher(Path dir, boolean recursive) {
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			this.keys = new HashMap<WatchKey, Path>();

			log.info("Scanning {} ...", dir);
			registerAll(dir);
		} catch (IOException e) {
			throw new FileWatchingException(e);
		}
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	void processEvents(FileWatchCallBack callback) {
		for (;;) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				log.error("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				@SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();

				if (kind == OVERFLOW) {
					continue;
				}

				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path path = dir.resolve(name);

				callback.handle(path);

				if (kind == ENTRY_CREATE) {
					try {
						if (Files.isDirectory(path, NOFOLLOW_LINKS)) {
							registerAll(path);
						}
					} catch (IOException x) {
						x.printStackTrace();
					}
				}
			}

			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

}