package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);
    private String method;
    private String path;
    private Map<String, String> params = new HashMap<>();

    public RequestLine(String requestLine) {
        log.debug("requestLine: {}", requestLine);

        String[] split = requestLine.split(" ");
        if (split.length != 3) {
            throw new IllegalArgumentException("Invalid request line: " + requestLine);
        }
        method = split[0];

        if (split[1].contains("?")) {
            int index = split[1].indexOf("?");
            path = split[1].substring(0, index);
            String query = split[1].substring(index + 1);
            params = HttpRequestUtils.parseQueryString(query);
        } else {
            path = split[1];
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
