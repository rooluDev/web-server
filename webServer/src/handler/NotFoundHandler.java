package handler;

import core.HttpRequest;
import core.HttpResponse;
import core.HttpUtils;

import java.io.File;
import java.io.FileInputStream;

/**
 * Not Found Handler
 */
public class NotFoundHandler implements RequestHandler {

    private final File notFoundFile = new File("resource/404.html");

    @Override
    public HttpResponse handle(HttpRequest request) throws Exception {
        byte[] content = new FileInputStream(notFoundFile).readAllBytes();
        HttpResponse response = new HttpResponse(404, "Not Found");
        response.setHeader("Content-Type", HttpUtils.getMimeType(HttpUtils.getMimeType(notFoundFile.getName())));
        response.setHeader("Content-Length", String.valueOf(content.length));
        response.setBody(content);
        return response;
    }
}
