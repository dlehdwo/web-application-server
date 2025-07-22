package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final DataOutputStream dos;
    private final Map<String, String> headers = new HashMap<String, String>();

    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }

    public void forward(String url) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            String extension = url.substring(url.lastIndexOf(".") + 1);
            log.debug("extension: {}", extension);
            addHeader("Content-Type", "text/" + extension + "; charset=utf-8");
            addHeader("Content-Length", Integer.toString(body.length));

            printHeader("200 OK");
            printBody(body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void forwardBody(String body) {
        try {
            byte[] contents = body.getBytes();
            headers.put("Content-Type", "text/html;charset=utf-8");
            headers.put("Content-Length", contents.length + "");
            printHeader("200 OK");
            printBody(contents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendRedirect(String url) {
        try {
            addHeader("Location", url);
            printHeader("302 Found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    private void printHeader(String status) throws IOException {
        dos.writeBytes("HTTP/1.1 " + status + "\r\n");
        for (String key : headers.keySet()) {
            dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
        }
        dos.writeBytes("\r\n");
        dos.flush();
    }

    private void printBody(byte[] body) throws IOException {
        dos.write(body, 0, body.length);
        dos.writeBytes("\r\n");
        dos.flush();
    }
}

