package core;

import handler.NotFoundHandler;
import handler.RequestHandler;
import handler.StaticFileHandler;
import handler.WasProxyHandler;

import java.util.regex.Pattern;

public class Router {

    private static final Pattern HAS_EXT_LAST_SEG = Pattern.compile("^(.*/)?[^./][^/]*\\.[^./][^/]*$");

    private static final Pattern ORIGIN_FORM = Pattern.compile("^(?:/(?:[A-Za-z0-9._~!$&'()*+,;=:@\\-]|%[0-9A-Fa-f]{2})+)*(?:\\?(?:[A-Za-z0-9._~!$&'()*+,;=:@/\\-?]|%[0-9A-Fa-f]{2})*)?$");

    private static final String API_PREFIX = "/api/";

    // 핸들러는 재사용 (매 요청 new 지양)
    private final RequestHandler staticHandler = new StaticFileHandler("resource"); // 정적 루트
    private final RequestHandler wasHandler = new WasProxyHandler("http://localhost:8081");
    private final RequestHandler notFound = new NotFoundHandler();

    public RequestHandler route(String rawPath) {
        // 0) 널/빈값 방어 및 기본값
        if (rawPath == null || rawPath.isEmpty()) rawPath = "/";

        // 1) origin-form 전체 유효성 (path + optional query). 아니면 404
        if (!ORIGIN_FORM.matcher(rawPath).matches()) {
            return notFound;
        }

        // 2) API 우선
        //    쿼리 포함 path에서 path 부분만 잘라 API 접두사 판단
        String pathOnly = stripQueryAndFragment(rawPath);
        if (pathOnly.startsWith(API_PREFIX)) {
            return wasHandler; // API 프록시
        }

        // 3) 정적 판단: 쿼리/프래그먼트 제거 후 “마지막 세그먼트 확장자” 체크
        if (HAS_EXT_LAST_SEG.matcher(pathOnly).matches()) {
            return staticHandler;
        }

        // 4) 그 외는 WAS(HTML 조합)
        return wasHandler;
    }

    // 쿼리/프래그먼트 제거 (정적 확장자 판정 전에 반드시 수행)
    private static String stripQueryAndFragment(String s) {
        int q = s.indexOf('?');
        if (q >= 0) s = s.substring(0, q);
        int h = s.indexOf('#');
        if (h >= 0) s = s.substring(0, h);
        if (s.isEmpty()) s = "/";
        return s;
    }
}