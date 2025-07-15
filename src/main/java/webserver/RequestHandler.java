package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            log.debug("header: {}", line);

            if (line == null) {
                return;
            }

            String[] split = line.split(" ");

            int contentLength = 0;
            String cookie = null;
            while (!line.isEmpty()) {
                line = bufferedReader.readLine();
                if (line.startsWith("Content-Length:")) {
                    String[] split1 = line.split(" ");
                    contentLength = Integer.parseInt(split1[1]);
                    log.debug("***Content-Length: {}", contentLength);
                } else if (line.startsWith("Cookie:")) {
                    cookie = line.split(" ")[1];
                }
                log.debug("reqeust line: {}", line);
            }

            DataOutputStream dos = new DataOutputStream(out);
            if (split[1].equals("/user/create")) {
                String data = IOUtils.readData(bufferedReader, contentLength);
                User user = signUpPost(data);
                users.add(user);
                log.debug("user: {}", user);
                response302Header(dos, "/index.html");
            } else if (split[1].equals("/user/login")) {
                String data = IOUtils.readData(bufferedReader, contentLength);
                login(data, dos);

            } else if (split[1].equals("/user/list")){
                System.out.println("cookie = " + cookie);
                boolean logined = false;
                if (cookie != null) {
                    logined = Boolean.parseBoolean(cookie.split("=")[1]);
                } else {
                    logined = false;
                }
                if (logined) {
                    StringBuilder userList = new StringBuilder();
                    for (User user : users) {
                        userList.append(user.toString()).append("\n");
                    }
                    response200Header(dos, "html", userList.length());
                    responseBody(dos, userList.toString().getBytes());
                } else {
                    response302Header(dos,"/user/login.html");
                }

            } else {
                byte[] body = Files.readAllBytes(new File("./webapp" + split[1]).toPath());
                String extension = getExtension(split[1]);
                response200Header(dos, extension, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void login(String data, DataOutputStream dos) {
        String[] split1 = data.split("&");
        String userId = null;
        String password = null;
        for (String param : split1) {
            String[] paramSplit = param.split("=");
            if(paramSplit[0].equals("userId")) {
                userId = paramSplit[1];
            } else if (paramSplit[0].equals("password")) {
                password = paramSplit[1];
            }
        }
        if (isOKUser(userId, password)) {
            response302HeaderWithCookie(dos, "/index.html", "logined=true");
        } else {
            response302HeaderWithCookie(dos, "/user/login_failed.html", "logined=false");
        }
    }

    public User signUpPost(String data) {
        String[] paramsSplit = data.split("&");
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
            dos.writeBytes("Content-Length: 0\r\n");
            dos.flush();
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

    private boolean isIndexHtml(String line) {
        String[] split = line.split(" ");
        return split[1].equals("/index.html");
    }

    private boolean isOKUser(String userId, String password) {
        for (User user : users) {
            if(user.getUserId().equals(userId) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    private String getExtension(String text) {
        int index = text.lastIndexOf(".");
        return text.substring(index + 1);
    }
}
