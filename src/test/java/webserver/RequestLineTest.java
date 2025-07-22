package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;


class RequestLineTest {

    @Test
    @DisplayName("메서드와 path 테스트하기")
    void create_method() {
        RequestLine requestLine = new RequestLine("GET /index.html HTTP/1.1");
        assertThat(requestLine.getMethod()).isEqualTo("GET");
        assertThat(requestLine.getPath()).isEqualTo("/index.html");

        requestLine = new RequestLine("POST /index.html HTTP/1.1");
        assertThat(requestLine.getMethod()).isEqualTo("POST");
        assertThat(requestLine.getPath()).isEqualTo("/index.html");
    }

    @Test
    @DisplayName("path와 params 테스트하기")
    void create_path_and_params() {
        RequestLine requestLine = new RequestLine("GET /user/create?userId=lee&password=dongjae HTTP/1.1");
        assertThat(requestLine.getMethod()).isEqualTo("GET");
        assertThat(requestLine.getPath()).isEqualTo("/user/create");
        assertThat(requestLine.getParams()).hasSize(2);
        assertThat(requestLine.getParams().get("userId")).isEqualTo("lee");
        assertThat(requestLine.getParams().get("password")).isEqualTo("dongjae");
    }
}