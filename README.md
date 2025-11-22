# 📋 미니멀 웹서버

---

## 📝 프로젝트 개요

이 프로젝트는 **웹서버**를 구현하는 것을 목표로 합니다.

- 요청 라인/헤더/바디를 처리하는 HTTP 파서와 응답 빌더 직접 구현
- 라우터로 정적/동적 요청 분기 (확장자/패턴 기반)
- 역프록시 핸들러로 WAS와 연동 (헤더 정리, Content-Length 설정)
- 웹서버의 PORT는 8080이고 WAS의 PORT는 8081로 구현

---

## 🛠 기술 스택

![Java](https://img.shields.io/badge/java-005F0F?style=for-the-badge&logo=java&logoColor=white)

---

## 📺 화면

- **정적파일 요청(localhost:8080/html/login.html)**  

<img width="1920" height="1080" alt="정적파일" src="https://github.com/user-attachments/assets/18b77b6d-595d-472d-99ee-a82c275eb1d2" />

- **HTML 요청(localhost:8080/member/list)**    

<img width="1920" height="1080" alt="was html" src="https://github.com/user-attachments/assets/41652736-7bcf-48e2-b587-489ccf8a354c" />


- **API GET(localhost:8080/api/members) 요청**  

<img width="1920" height="1080" alt="api get" src="https://github.com/user-attachments/assets/0036b631-fca9-4363-be6c-83aefbe15b56" />


- **API POST(login form)요청** 

https://github.com/user-attachments/assets/70d2a4f8-383e-48c5-ba47-25869610ce1b


---

## 💡 주안점

### HTTP 요청/응답 파서 직접 구현

HTTP/1.1 스펙을 참고해 **요청 라인, 헤더, 바디를 직접 파싱하는 HttpRequestParser**를 구현했습니다.  
라이브러리에 의존하지 않고, 소켓에서 넘어온 바이트 스트림을 한 줄씩 읽어가며 아래 순서로 처리합니다.

<details>
  <summary>코드 보기 (펼치기/접기)</summary>

HttpRequestParser Start Line Parse

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

 HttpRequestParser Header Line Parse
  
      while (true) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) break;
            int index = line.indexOf(':');
            String key = line.substring(0, index).trim();
            String value = line.substring(index + 1).trim();
            request.getHeaders().put(key, value);
        }

HttpRequestParser Body Line Parse
  
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
  

  

  [HttpRequestParser 전체 코드](https://github.com/rooluDev/web-server/blob/main/webServer/src/core/HttpRequestParser.java)
  </details>

---
### Path Router를 이용한 handler 처리

HTTP 요청의 path 형태를 기반으로 정적 리소스, API 요청, 그 외 요청(WAS 위임) 을 구분하기 위해 간단한 라우터를 직접 구현했습니다.
정규식을 사용해 브라우저가 요청한 path가 유효한 HTTP origin-form인지, 정적 파일인지, API 요청인지를 판별하고 그에 맞는 Handler를 반환합니다.

<details>
  <summary>코드 보기 (펼치기/접기)</summary>

Router
    
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
    

  [Router 전체 코드](https://github.com/rooluDev/web-server/blob/main/webServer/src/core/Router.java)
  </details>

---
### 정적 리소스 처리

클라이언트가 요청한 경로를 실제 서버의 파일 시스템 경로로 매핑한 뒤,  
HTML/CSS/JS/이미지 등의 정적 파일을 직접 읽어 **HTTP 응답으로 구성**합니다.

<details>
  <summary>코드 보기 (펼치기/접기)</summary>

StaticFileHandler
    
        // 경로 설정
        String path = request.getPath();
        String rel = path.startsWith("/") ? path.substring(1) : path;
        Path file = root.resolve(rel).normalize();

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

  [StaticFileHandler 전체 코드](https://github.com/rooluDev/web-server/blob/main/webServer/src/handler/StaticFileHandler.java)
  </details>

---
### WAS 연동 리버스 프록시로 클라이언트 요청을 Spring Boot API 서버로 전달

웹서버에서 API 요청을 받으면 이를 내부 Spring Boot WAS로 전달하는 **리버스 프록시(reverse proxy)** 기능을 직접 구현했습니다.

<details>
  <summary>코드 보기 (펼치기/접기)</summary>

URL 설정
    
        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(request.getMethod());
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);

Was HttpRequest Header 설정
        
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

Was HttpResponse 호출 및 응답

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

  [WasProxyHandler 전체 코드](https://github.com/rooluDev/web-server/blob/main/webServer/src/handler/WasProxyHandler.java)
  </details>
