package nl.jssl.codewatch;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import nl.jssl.codewatch.websocket.CodeUpdateWebsocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableAutoConfiguration
@EnableWebSocket
@Controller
public class Main implements WebSocketConfigurer {
    private String sourcePath = "D:/workspaces/trc/trunk/common/core/src/java/";
    private String binpath = "d:/workspaces/trc/trunk/common/core/build/classes/main";
    @Autowired
    private CodeRepositoryWatcher codeRepositoryWatcher;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(codeUpdateWebsocketHandler(), "/sourcecodeHandler");
    }

    @PostConstruct
    public void startWatchdaemon() throws Exception {
        codeRepositoryWatcher.watch();
    }

    @RequestMapping("/code/{class}")
    @ResponseBody
    public Map<String, String> javaclass(@PathVariable("class") String classname) {
        return codeRepositoryWatcher.getReport().get(classname);
    }

    @RequestMapping("/code")
    @ResponseBody
    public Set<String> javaclasses() {
        return codeRepositoryWatcher.getReport().keySet();
    }

    @Bean
    public CodeRepositoryWatcher codeRepositoryWatcher() {
        CodeRepositoryWatcher watcher = new CodeRepositoryWatcher(sourcePath, binpath);
        watcher.registerCodeAnalyser(new FindbugsAnalyser());
        return watcher;
    }

    @Bean
    public CodeUpdateWebsocketHandler codeUpdateWebsocketHandler() {
        return new CodeUpdateWebsocketHandler();
    }
}
