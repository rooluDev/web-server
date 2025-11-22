package core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Http Response
 */
public class HttpResponse {
    private int statusCode;
    private String statusText;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public HttpResponse(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public void setHeader(String key, String value) { headers.put(key, value); }
    public void setBody(byte[] body) { this.body = body; }

    public void send(OutputStream out) throws IOException {

        StringBuilder response = new StringBuilder();

        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");
        headers.put("Content-Length", String.valueOf(body.length));
        headers.putIfAbsent("Connection", "close");

        for (Map.Entry<String, String> h : headers.entrySet()) {
            response.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
        }

        response.append("\r\n");

        out.write(response.toString().getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

}
