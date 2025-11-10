package core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResponse {
    private int statusCode;
    private String statusText;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public HttpResponse(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public HttpResponse(int statusCode, String notFound, String contentType, byte[] bytes) {
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

//        logToConsole(response.toString(), body, headers.get("Content-Type"));

        out.write(response.toString().getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private static final int LOG_BODY_LIMIT = 2048; // 과도한 로그 방지

    private void logToConsole(String head, byte[] body, String contentType) {
        System.out.println("----- HTTP RESPONSE (wire) -----");
        System.out.print(head);

        boolean looksText = looksTextual(contentType);
        if (looksText) {
            Charset cs = detectCharset(contentType);
            String text = safePreview(body, cs, LOG_BODY_LIMIT);
            System.out.println(text);
            if (body.length > LOG_BODY_LIMIT) {
                System.out.printf("... [truncated, total %d bytes]%n", body.length);
            }
        } else {
            // 바이너리는 길이 + 앞부분 hex 미리보기
            System.out.printf("<binary body: %d bytes>%n", body.length);
            int n = Math.min(body.length, 64);
            if (n > 0) {
                StringBuilder hex = new StringBuilder(n * 3);
                for (int i = 0; i < n; i++) {
                    hex.append(String.format("%02X ", body[i]));
                }
                System.out.println("hex preview: " + hex.toString().trim() + (body.length > n ? " ..." : ""));
            }
        }
        System.out.println("----- END RESPONSE -----");
    }

    private static boolean looksTextual(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase(Locale.ROOT);
        return ct.startsWith("text/")
                || ct.contains("json")
                || ct.contains("xml")
                || ct.contains("html")
                || ct.contains("javascript")
                || ct.contains("svg")
                || ct.contains("csv");
    }

    private static Charset detectCharset(String contentType) {
        if (contentType != null) {
            Matcher m = Pattern.compile("charset=([^;]+)", Pattern.CASE_INSENSITIVE).matcher(contentType);
            if (m.find()) {
                String enc = m.group(1).trim().replace("\"", "");
                try { return Charset.forName(enc); } catch (Exception ignore) {}
            }
        }
        return StandardCharsets.UTF_8; // 기본
    }

    private static String safePreview(byte[] body, Charset cs, int limit) {
        if (body.length <= limit) return new String(body, cs);
        // 멀티바이트 경계 깨짐 최소화를 위해 살짝 여유를 둬도 됨(여기선 단순히 잘라서 표기)
        return new String(Arrays.copyOf(body, limit), cs);
    }
}
