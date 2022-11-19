package com.gmail.at.kotamadeo;

import com.gmail.at.kotamadeo.server.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.gmail.at.kotamadeo.server.util.ConsoleHelper.sendResponseOk;
import static com.gmail.at.kotamadeo.server.util.ServerUtil.getAllowedMethods;
import static com.gmail.at.kotamadeo.server.util.ServerUtil.getValidPaths;

public class Main {
    private static final List<String> VALID_PATHS = getValidPaths();
    private static final List<String> ALLOWED_METHODS = getAllowedMethods();

    public static void main(String[] args) {
        Server server = new Server();
        addSomeHandler(server, "/index.html", "GET");
        addSomeHandler(server, "/classic.html", "GET");
        addSomeHandler(server, "/spring.svg", "GET");
        addSomeHandler(server, "/default-get.html", "GET");
        addSomeHandler(server, "/index.html", "POST");
        addSomeHandler(server, "/classic.html", "POST");
        addSomeHandler(server, "/spring.svg", "POST");
        addSomeHandler(server, "/default-get.html", "POST");
        server.init();
    }
    
    private static void addSomeHandler(Server server, String path, String method) {
        String file = Main.VALID_PATHS.stream().filter(p -> p.equals(path))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        String allowMethod = Main.ALLOWED_METHODS.stream().filter(m -> m.equals(method))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        server.addHandler(allowMethod, file, (request, out) -> {
            try {
                Path filePath = Path.of("src/main/resources", "/static", request.getPath());
                String mimeType = Files.probeContentType(filePath);
                long length = Files.size(filePath);
                out.write(sendResponseOk(mimeType, length));
                Files.copy(filePath, out);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
