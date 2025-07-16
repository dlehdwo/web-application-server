package webserver;


import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RequestHandlerTest {

    RequestHandler requestHandler;

    @BeforeEach
    void setUp() {
        requestHandler = new RequestHandler(null);
    }

//    @Test
//    @DisplayName("요구사항2 Get으로 회원가입하기")
//    void Get으로회원가입하기() {
//        User answer = requestHandler.signUpGet("/user/create?userId=aaa&password=bbb&name=dongjae&email=lee%40gmail");
//        User expected = new User("aaa", "bbb", "dongjae", "lee%40gmail");
//        assertThat(answer).usingRecursiveComparison().isEqualTo(expected);
//    }
//
//    @Test
//    @DisplayName("요구사항2 POST으로 회원가입하기")
//    void POST으로회원가입하기() {
//        User answer = requestHandler.signUpPost("userId=aaa&password=bbb&name=dongjae&email=lee%40gmail");
//        User expected = new User("aaa", "bbb", "dongjae", "lee%40gmail");
//        assertThat(answer).usingRecursiveComparison().isEqualTo(expected);
//    }
}