package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

class HttpRequestTest {

    private String testDirectory = "./src/test/resources/";

    @Test
    @DisplayName("GET request 테스트")
    void request_GET() throws Exception {
        HttpRequest httpRequest = new HttpRequest(createInputStream("Http_GET.txt"));

        assertThat(httpRequest.getMethod()).isEqualTo("GET");
        assertThat(httpRequest.getPath()).isEqualTo("/user/create");
        assertThat(httpRequest.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(httpRequest.getParameter("userId")).isEqualTo("lee");
    }

    @Test
    @DisplayName("POST request 테스트")
    void request_POST() throws Exception {
        HttpRequest httpRequest = new HttpRequest(createInputStream("Http_POST.txt"));

        assertThat(httpRequest.getMethod()).isEqualTo("POST");
        assertThat(httpRequest.getPath()).isEqualTo("/user/create");
        assertThat(httpRequest.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(httpRequest.getParameter("userId")).isEqualTo("lee");
    }

    private InputStream createInputStream(String filename) throws FileNotFoundException {
        return new FileInputStream(new File(testDirectory + filename));
    }

}