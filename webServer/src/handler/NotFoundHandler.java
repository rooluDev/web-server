package handler;

import core.HttpRequest;
import core.HttpResponse;
import core.HttpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NotFoundHandler implements RequestHandler {

    private final File notFoundFile = new File("resource/404.html");

    @Override
    public HttpResponse handle(HttpRequest request) {
        try {
            byte[] content = new FileInputStream(notFoundFile).readAllBytes();
            HttpResponse response = new HttpResponse(404, "Not Found");
            response.setHeader("Content-Type", HttpUtils.getMimeType(HttpUtils.getMimeType(notFoundFile.getName())));
            response.setHeader("Content-Length", String.valueOf(content.length));
            response.setBody(content);
            return response;
        } catch (IOException e) {
            byte[] content = "<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8);
            HttpResponse response = new HttpResponse(404, "Not Found");
            response.setHeader("Content-Type", "text/html; charset=UTF-8");
            response.setHeader("Content-Length", String.valueOf(content.length));
            response.setBody(content);
            return response;
        }
    }
}
