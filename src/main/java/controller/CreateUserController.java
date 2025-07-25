package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.RequestHandler;

public class CreateUserController implements Controller{
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);


    @Override
    public void service(HttpRequest request, HttpResponse response) {
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email"));
        DataBase.addUser(user);
        log.debug("user: {}", user);
        response.sendRedirect("/index.html");
    }
}
