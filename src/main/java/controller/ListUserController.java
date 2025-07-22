package controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Collection;

public class ListUserController implements Controller {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        if (!request.isLogin()) {
            response.sendRedirect("/user/login.html");
        }

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
        response.forwardBody(sb.toString());
    }
}
