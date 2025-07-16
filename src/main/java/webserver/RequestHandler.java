package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static List<User> users = new ArrayList<User>();

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
            log.debug("request line: {}", line);

            if (line == null) {
                return;
            }

            String[] tokens = line.split(" ");
            String method = tokens[0];
            String url = tokens[1];

            int contentLength = 0;
            Boolean cookie = false;
            while (!line.isEmpty()) {
                line = bufferedReader.readLine();
                log.debug("header: {}", line);
                if (line.startsWith("Content-Length:")) {
                    contentLength = getContentLength(line);
                } else if (line.startsWith("Cookie:")) {
                    cookie = getCookie(line);
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            if (url.equals("/user/create")) {
                String body = IOUtils.readData(bufferedReader, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);
                log.debug("user: {}", user);
                response302Header(dos, "/index.html");
            } else if (url.equals("/user/login")) {
                String body = IOUtils.readData(bufferedReader, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = DataBase.findUserById(params.get("userId"));
                if (user == null) {
                    response302HeaderWithCookie(dos, "/user/login_failed.html", "logined=false");
                    return;
                }
                if (user.getPassword().equals(params.get("password"))) {
                    response302HeaderWithCookie(dos, "/index.html", "logined=true");
                } else {
                    response302HeaderWithCookie(dos, "/user/login_failed.html", "logined=false");
                }
            } else if (url.equals("/user/list")){
                if (cookie) {
                    Collection<User> all = DataBase.findAll();
                    StringBuilder sb = new StringBuilder();
                    sb.append("<table border='1'>");
                    for (User user : all) {
                        sb.append("<tr>");
                        sb.append("<td>" + user.getUserId() + "</td>");
                        sb.append("<td>" + user.getName() + "</td>");
                        sb.append("<td>" + user.getEmail() + "</td>");
                        sb.append("<tr>");
                    }
                    sb.append("</table>");
                    response200Header(dos, "html", sb.length());
                    responseBody(dos, sb.toString().getBytes());
                } else {
                    response302Header(dos,"/user/login.html");
                    return;
                }

            } else {
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                String extension = getExtension(url);
                response200Header(dos, extension, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static Boolean getCookie(String line) {
        String cookie = line.split(":")[1].trim();
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookie);
        String value = cookies.get("logined");
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return false;
    }

    private static int getContentLength(String line) {
        int contentLength;
        String[] split = line.split(":");
        contentLength = Integer.parseInt(split[1].trim());
        return contentLength;
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String url, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("Content-Length: 0\r\n");
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, String extension, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/"+extension+";charset=utf-8\r\n");
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

    private String getExtension(String text) {
        int index = text.lastIndexOf(".");
        return text.substring(index + 1);
    }
}
