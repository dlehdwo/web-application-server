package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Map<String, String> header = new HashMap<>();
    private Map<String, String> parameters;
    private RequestLine requestLine;

    public HttpRequest(InputStream inputStream) {
        try {
            parseHttpRequest(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseHttpRequest(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line = bufferedReader.readLine();

        if (line == null) {
            return;
        }

        requestLine = new RequestLine(line);

        while (true) {
            line = bufferedReader.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            log.debug("line: {}", line);

            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
            header.put(pair.getKey(), pair.getValue());
        }

        if ("POST".equals(getMethod())) {
            String body = IOUtils.readData(bufferedReader, Integer.parseInt(header.get("Content-Length")));
            parameters = HttpRequestUtils.parseQueryString(body);
        } else {
            parameters = requestLine.getParams();
        }

    }

    public String getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String key) {
        return header.get(key);
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }
}
