package handler;

import core.HttpRequest;
import core.HttpResponse;
import core.HttpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class StaticFileHandler implements RequestHandler {
    private final Path root;
    private final File notFoundFile = new File("resource/404.html");
    private static final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

    public StaticFileHandler(String rootDirectory) {
        this.root = Paths.get(rootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public HttpResponse handle(HttpRequest request) {

        // 경로 설정
        String path = request.getPath();
        String rel = path.startsWith("/") ? path.substring(1) : path;
        Path file = root.resolve(rel).normalize();

        try {
            // 파일 크기 읽기
            byte[] body = Files.readAllBytes(file);

            // response 헤더 설정
            String mime = HttpUtils.getMimeType(file.getFileName().toString());
            String date = formatter.format(Instant.now());
            String lastModified = formatter.format(Files.getLastModifiedTime(file).toInstant());
            String etag = "W/\"" + body.length + "-" + Files.getLastModifiedTime(file).toMillis() + "\"";

            HttpResponse response = new HttpResponse(200, "OK");
            response.setHeader("Date", date);
            response.setHeader("Content-Type", mime);
            response.setHeader("Content-Length", String.valueOf(body.length));
            response.setHeader("Last-Modified", lastModified);
            response.setHeader("ETag", etag);
            response.setHeader("Cache-Control", "public, max-age=300");
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Connection", "close"); // 지금 구조에선 close가 깔끔
            response.setBody(body);

            return response;
        } catch (IOException e) {
            return return404();
        }
    }

    private HttpResponse return404() {
        // resource/404.html
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
