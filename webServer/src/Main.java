import core.*;
import handler.NotFoundHandler;
import handler.RequestHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(32, Runtime.getRuntime().availableProcessors() * 4));
        Router router = new Router();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ MiniWebServer running on port " + PORT);
            while (true) {
                Socket client = serverSocket.accept();
                pool.execute(() -> {
                    try {
                        handleClient(client, router);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    private static void handleClient(Socket client, Router router) throws IOException {
        try {
            // request parse 후 HttpRequest 객체 반환
            HttpRequest request = HttpRequestParser.parse(client.getInputStream());
            // path pattern에 따라 맞는 handler 반환
            RequestHandler requestHandler = router.route(request.getPath());
            // request 처리 후 response
            HttpResponse response = requestHandler.handle(request);
            // response send
            response.send(client.getOutputStream());

            client.close();
        } catch (Exception e) {
            sendError404(client.getOutputStream());
        }
    }

    private static void sendError404(OutputStream out) throws IOException {
        File f = new File("resource/404.html");
        byte[] body = Files.readAllBytes(f.toPath());

        HttpResponse resp = new HttpResponse(404, "Not Found");
        resp.setHeader("Content-Type", "text/html; charset=UTF-8");
        resp.setHeader("Content-Length", String.valueOf(body.length));
        resp.setBody(body);

        resp.send(out);
    }
}
