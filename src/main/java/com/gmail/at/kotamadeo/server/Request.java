package com.gmail.at.kotamadeo.server;

import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.join;
import static java.net.URI.create;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.http.client.utils.URLEncodedUtils.parse;

@Getter
public class Request {
    private final String method;
    private final String requestTarget;
    private final String path;
    private final URI uri;
    private final String protocol;
    private final List<String> headers;
    private final String body;

    public Request(String method, String requestTarget, String protocol, List<String> headers, String body) {
        this.method = method;
        this.requestTarget = requestTarget;
        uri = create(this.requestTarget);
        path = uri.getPath();
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
    }

    public List<NameValuePair> getQueryParams() {
        return parse(uri, UTF_8);
    }

    public List<NameValuePair> getQueryParam(String name) {
        List<NameValuePair> parsedQuery = parse(uri, UTF_8);
        return parsedQuery.stream()
                .filter(o -> o.getName().equals(name))
                .toList();
    }


    @Override
    public String toString() {
        return """
                Method: %s
                Request-target: %s
                Path: %s
                Query: %s
                HTTP-version: %s
                FIELD-LINES:
                %s
                                
                %s
                """.formatted(method, requestTarget, path, getQuery(), protocol, join("\n", headers), body);
    }

    private String getQuery() {
        return uri.getQuery();
    }
}
