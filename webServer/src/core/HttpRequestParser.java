package core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class HttpRequestParser {

    public static HttpRequest parse(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        HttpRequest request = new HttpRequest();

        // start line parse
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            return request;
        }

        String[] parts = requestLine.split(" ", 3);

        String method = parts[0];
        String fullPath = parts[1];
        String version = parts[2];

        int q = fullPath.indexOf("?");
        String path = (q >= 0) ? fullPath.substring(0, q) : fullPath;
        String query = (q >= 0) ? fullPath.substring(q + 1)  : null;

        request.setMethod(method);
        request.setPath(path);
        request.setQuery(query);
        request.setVersion(version);

        // header line parse
        while (true) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) break;
            int index = line.indexOf(':');
            String key = line.substring(0, index).trim();
            String value = line.substring(index + 1).trim();
            request.getHeaders().put(key, value);
        }

        // body parse
        String transferEncoding = request.getHeaders().get("Transfer-Encoding");
        String contentLength = request.getHeaders().get("Content-Length");

        byte[] bodyBytes = new byte[0];

        if (transferEncoding != null && transferEncoding.toLowerCase(java.util.Locale.ROOT).contains("chunked")) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(512);
            while (true) {
                String sizeLine = reader.readLine();
                int semi = (sizeLine != null) ? sizeLine.indexOf(';') : -1;
                String hex = (semi >= 0) ? sizeLine.substring(0, semi) : sizeLine;
                int size = Integer.parseInt(hex.trim(), 16);
                if (size == 0) {
                    String t;
                    while ((t = reader.readLine()) != null && !t.isEmpty()) {}
                    break;
                }
                char[] cbuf = new char[size];
                int read = 0;
                while (read < size) {
                    int n = reader.read(cbuf, read, size - read);
                    if (n == -1) throw new IOException("unexpected EOF in chunk");
                    read += n;
                }
                reader.read();
                reader.read();
                for (int i = 0; i < size; i++) out.write((byte)(cbuf[i] & 0xFF));
            }
            bodyBytes = out.toByteArray();
        } else if (contentLength != null) {
            int len = Integer.parseInt(contentLength.trim());
            char[] cbuf = new char[len];
            int read = 0;
            while (read < len) {
                int n = reader.read(cbuf, read, len - read);
                if (n == -1) break;
                read += n;
            }
            bodyBytes = new byte[read];
            for (int i = 0; i < read; i++) bodyBytes[i] = (byte)(cbuf[i] & 0xFF);
        }

        request.setBody(bodyBytes);

        return request;
    }
}
