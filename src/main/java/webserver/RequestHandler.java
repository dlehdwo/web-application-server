package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line = bufferedReader.readLine();
            log.debug("header: {}", line);

            if (line == null) {
                return ;
            }

            String[] split = line.split(" ");

            while (!line.isEmpty()) {
                line = bufferedReader.readLine();
                log.debug("reqeust line: {}", line);
            }

            if (split[1].startsWith("/user/create")) {
                User user = signUpGet(split[1]);
            } else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + split[1]).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public User signUpGet(String url) {
        int index = url.indexOf("?");
        String requestPath = url.substring(0, index);
        String params = url.substring(index + 1);

        String[] paramsSplit = params.split("&");
        String userId = null;
        String password = null;
        String name = null;
        String email = null;
        for (String param : paramsSplit) {
            String[] paramSplit = param.split("=");
            if(paramSplit[0].equals("userId")) {
                userId = paramSplit[1];
            } else if (paramSplit[0].equals("password")) {
                password = paramSplit[1];
            } else if (paramSplit[0].equals("name")) {
                name = paramSplit[1];
            } else if (paramSplit[0].equals("email")) {
                email = paramSplit[1];
            }
        }
        return new User(userId, password, name, email);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isIndexHtml(String line) {
        String[] split = line.split(" ");
        return split[1].equals("/index.html");
    }
}
