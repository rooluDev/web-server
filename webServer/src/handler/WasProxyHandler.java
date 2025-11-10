package handler;

import core.HttpRequest;
import core.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class WasProxyHandler implements RequestHandler {

    private final String wasBaseUrl;

    public WasProxyHandler(String wasBaseUrl) {
        this.wasBaseUrl = wasBaseUrl;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        try {
            String path = "";
            if(request.getQuery() != null) {
                path = wasBaseUrl + request.getPath() + request.getQuery();
            }else {
                path = wasBaseUrl + request.getPath();
            }

            // URL 설정
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(request.getMethod());
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);

            // header setting
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                if (!header.getKey().equalsIgnoreCase("Host") && !header.getKey().equalsIgnoreCase("Content-Length")) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            // body setting
            if (request.getMethod().equals("POST") || request.getMethod().equals("PUT") || request.getMethod().equals("PATCH")) {
                connection.setDoOutput(true);
                byte[] requestBody = request.getBody();
                connection.setRequestProperty("Content-Length", String.valueOf(requestBody.length));
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestBody);
                }
            }

            // response 호출 및 응답
            int status = connection.getResponseCode();
            InputStream inputStream = (status < 400) ? connection.getInputStream() : connection.getErrorStream();
            byte[] responseBody = (inputStream != null) ? inputStream.readAllBytes() : new byte[0];

            HttpResponse httpResponse = new HttpResponse(status, connection.getResponseMessage());

            for (Map.Entry<String, java.util.List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null && !header.getValue().isEmpty()) {
                    if (header.getKey().equalsIgnoreCase("Transfer-Encoding")
                            || header.getKey().equalsIgnoreCase("Keep-Alive")) continue;

                    httpResponse.setHeader(header.getKey(), header.getValue().get(0));
                }
            }


            httpResponse.setBody(responseBody);

            return httpResponse;
        } catch (IOException e) {
            e.printStackTrace();
            HttpResponse errorResponse = new HttpResponse(502, "Bad Gateway");
            errorResponse.setHeader("Content-Type", "text/plain");
            errorResponse.setBody("Failed to connect to WAS".getBytes());
            return errorResponse;
        }
    }
}
