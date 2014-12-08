package nl.jssl.codewatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.jssl.codewatch.websocket.CodeUpdateWebsocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CodeRepositoryWatcher {
    private final static Logger log = LoggerFactory.getLogger(CodeRepositoryWatcher.class);

    private Map<String, Map<String, String>> report = new HashMap<>();
    private List<CodeAnalyser> analysers = new ArrayList<>();
    @Autowired
    private CodeUpdateWebsocketHandler codeUpdateWebsocketHandler;
    private String sourcePath;
    private String binpath;
    private long lastTime;

    public CodeRepositoryWatcher(String sourcePath, String binpath) {
        super();
        this.sourcePath = sourcePath;
        this.binpath = binpath;
    }

    public void registerCodeAnalyser(CodeAnalyser codeAnalyser) {
        analysers.add(codeAnalyser);
    }

    public void watch() {
        try {
            DirectoryWatcher.watch(sourcePath, new FileWatchCallBack() {
                @Override
                public void handle(Path path) {
                    long currentTime = System.currentTimeMillis();
                    if ((currentTime - lastTime) > 1000) {// ignore spurious events (probably caused by eclipse)
                        String sourcefile = path.toString().substring(sourcePath.length());
                        String classFile = getClassFile(sourcefile);

                        String analysis = analyse(classFile);
                        String code = read(path);
                        Map<String, String> result = new HashMap<>();
                        result.put("code", code);
                        result.put("analysis", analysis);
                        String classname = getClassName(sourcefile);
                        report.put(classname, result);
                        log.debug("update client");
                        codeUpdateWebsocketHandler.send(classname);
                    }
                    lastTime = currentTime;
                }

                private String read(Path path) {
                    try {
                        return new String(Files.readAllBytes(path));
                    } catch (IOException e) {
                        throw new AnalysisException(e);
                    }
                }

                private String getClassName(String sourcefile) {
                    return sourcefile.replace("\\", ".").replace("/", ".");
                }

                private String getClassFile(String sourcefile) {
                    String classFile = sourcefile.substring(0, sourcefile.length() - 5);
                    classFile = binpath + "/" + classFile + ".class";
                    return classFile;
                }

                private String analyse(String classFile) {
                    String result = "";
                    for (CodeAnalyser codeAnalyser : analysers) {
                        result += codeAnalyser.analyse(classFile) + "\n";
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            throw new AnalysisException(e);
        }
    }

    public Map<String, Map<String, String>> getReport() {
        return report;
    }
}
