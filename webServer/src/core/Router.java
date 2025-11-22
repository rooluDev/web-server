package core;

import handler.NotFoundHandler;
import handler.RequestHandler;
import handler.StaticFileHandler;
import handler.WasProxyHandler;

import java.util.regex.Pattern;

/**
 * Router
 */
public class Router {

    private static final Pattern HAS_EXT_LAST_SEG = Pattern.compile("^(.*/)?[^./][^/]*\\.[^./][^/]*$");

    private static final Pattern ORIGIN_FORM = Pattern.compile("^(?:/(?:[A-Za-z0-9._~!$&'()*+,;=:@\\-]|%[0-9A-Fa-f]{2})+)*(?:\\?(?:[A-Za-z0-9._~!$&'()*+,;=:@/\\-?]|%[0-9A-Fa-f]{2})*)?$");

    private static final String API_PREFIX = "/api/";

    private final RequestHandler staticHandler = new StaticFileHandler("resource"); // 정적 루트
    private final RequestHandler wasHandler = new WasProxyHandler("http://localhost:8081");
    private final RequestHandler notFound = new NotFoundHandler();

    public RequestHandler route(String rawPath) {

        // 널값 방어
        if (rawPath == null || rawPath.isEmpty()) rawPath = "/";


        if (!ORIGIN_FORM.matcher(rawPath).matches()) {
            return notFound;
        }

        // API
        String pathOnly = stripQueryAndFragment(rawPath);
        if (pathOnly.startsWith(API_PREFIX)) {
            return wasHandler;
        }

        // 정적 파일 요청
        if (HAS_EXT_LAST_SEG.matcher(pathOnly).matches()) {
            return staticHandler;
        }

        // 그 외
        return wasHandler;
    }

    // 쿼리,프래그먼트 제거
    private static String stripQueryAndFragment(String fullPath) {
        int q = fullPath.indexOf('?');
        if (q >= 0) fullPath = fullPath.substring(0, q);
        int h = fullPath.indexOf('#');
        if (h >= 0) fullPath = fullPath.substring(0, h);
        if (fullPath.isEmpty()) fullPath = "/";
        return fullPath;
    }
}