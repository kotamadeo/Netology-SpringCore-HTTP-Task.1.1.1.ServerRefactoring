package com.gmail.at.kotamadeo.server.util;

import lombok.experimental.UtilityClass;

import static java.nio.charset.StandardCharsets.UTF_8;

@UtilityClass
public class ConsoleHelper {

    private byte[] sendResponseOk() {
        return """
                HTTP/1.1 200 OK
                Content-Length: 0
                Connection: close
                                
                                
                """.getBytes(UTF_8);
    }

    public static byte[] sendResponseOk(String mimeType, long length) {
        return """
                HTTP/1.1 200 OK
                Content-Type: %s
                Content-Length: %s
                Connection: close
                                        
                                        
                """.formatted(mimeType, length).getBytes();
    }

    public static byte[] sendResponseNotFound() {
        return """
                HTTP/1.1 404 Not Found
                Content-Length: 0
                Connection: close
                """.getBytes(UTF_8);
    }

    public static byte[] sendResponseBadRequest() {
        return """
                HTTP/1.1 400 Bad Request
                Content-Length: 0
                Connection: close
                """.getBytes(UTF_8);
    }
}
