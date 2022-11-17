package com.gmail.at.kotamadeo.server;

import com.gmail.at.kotamadeo.server.util.ServerUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gmail.at.kotamadeo.server.util.ConsoleHelper.sendResponseBadRequest;
import static com.gmail.at.kotamadeo.server.util.ConsoleHelper.sendResponseNotFound;
import static com.gmail.at.kotamadeo.server.util.ServerUtil.getPropertyByKey;
import static java.lang.Integer.parseInt;

@Getter
@Setter
public class Server {
    private int port;
    private int threadPoolSize;
    private final ExecutorService threadPool;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server() {
        this.port = parseInt(getPropertyByKey("server.port"));
        this.threadPoolSize = parseInt(getPropertyByKey("server.threadPoolSize"));
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void init() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> processingRequest(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processingRequest(Socket socket) {
        try (BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            Request request = parseRequest(in);
            if (request == null) {
                out.write(sendResponseBadRequest());
            } else {
                if (!handlers.containsKey(request.getMethod())
                        || !handlers.get(request.getMethod()).containsKey(request.getPath())) {
                    out.write(sendResponseNotFound());
                } else {
                    handlers.get(request.getMethod())
                            .get(request.getPath())
                            .handle(request, out);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.putIfAbsent(method, new ConcurrentHashMap<>());
        handlers.get(method).put(path, handler);
    }

    private Request parseRequest(BufferedInputStream in) throws IOException {
        in.mark(parseInt(getPropertyByKey("server.requestHeaderSize")));
        byte[] buffer = new byte[parseInt(getPropertyByKey("server.requestHeaderSize"))];
        int read = in.read(buffer);
        byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
        int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        List<String> allowedMethods = ServerUtil.getAllowedMethods();
        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            return null;
        }

        String requestTarget = requestLine[1];
        if (!requestTarget.startsWith("/")) {
            return null;
        }

        String protocol = requestLine[2];
        if (!protocol.startsWith("HTTP")) {
            return null;
        }

        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);

        if (headersEnd == -1) {
            return null;
        }
        in.reset();
        in.skip(headersStart);

        byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
        List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        String body = null;
        if (!method.equals(getPropertyByKey("server.methodGet"))) {
            in.skip(headersDelimiter.length);
            Optional<String> contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                body = new String(bodyBytes);
            }
        }
        Request request = new Request(method, requestTarget, protocol, headers, body);
        String result = """
                %s
                Query params: %s
                Query params named "value": %s
                Query params named: "title": %s
                """.formatted(
                request
                , request.getQueryParams()
                , request.getQueryParam("value")
                , request.getQueryParam("title"));
        System.out.println(result);
        return request;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
