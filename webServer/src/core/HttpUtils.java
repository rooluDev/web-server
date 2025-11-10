package core;

import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("txt", "text/plain");
    }

    public static String getMimeType(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx != -1) {
            String ext = filename.substring(idx + 1).toLowerCase();
            return MIME_TYPES.getOrDefault(ext, "application/octet-stream");
        }
        return "application/octet-stream";
    }
}
