import core.HttpRequest;
import core.HttpRequestParser;
import core.HttpResponse;
import core.Router;
import handler.RequestHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
                pool.execute(() -> handleClient(client, router));
            }
        }
    }

    private static void handleClient(Socket client, Router router) {
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
            e.printStackTrace();
        }
    }
}
