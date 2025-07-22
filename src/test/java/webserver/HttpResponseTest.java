package webserver;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.*;

public class HttpResponseTest {
    private final String testDirectory = "./src/test/resources/";

    @Test
    @DisplayName("forward 메서드 테스트")
    public void responseForward() throws Exception {
        //given
        HttpResponse httpResponse = new HttpResponse(createOutputStream("Http_Forward.txt"));
        //when
        httpResponse.forward("/index.html");
        //then
        File answerFile = new File(testDirectory + "Http_Forward.txt");
        File requiredFile = new File(testDirectory + "Answer_Http_Forward.txt");
        assertThat(answerFile).hasSameTextualContentAs(requiredFile);
    }

    @Test
    public void responseRedirect() throws Exception {
        //given
        HttpResponse httpResponse = new HttpResponse(createOutputStream("Http_Redirect.txt"));
        //when
        httpResponse.sendRedirect("/index.html");
        //then
        File answerFile = new File(testDirectory + "Http_Redirect.txt");
        File requiredFile = new File(testDirectory + "Answer_Http_Redirect.txt");
        assertThat(answerFile).hasSameTextualContentAs(requiredFile);
    }

    @Test
    public void responseCookies() throws Exception {
        //given
        HttpResponse httpResponse = new HttpResponse(createOutputStream("Http_Cookies.txt"));
        //when
        httpResponse.addHeader("Set-Cookie", "logined=true");
        httpResponse.sendRedirect("/index.html");
        //then
        File answerFile = new File(testDirectory + "Http_Cookies.txt");
        File requiredFile = new File(testDirectory + "Answer_Http_Cookies.txt");
        assertThat(answerFile).hasSameTextualContentAs(requiredFile);
    }

    private OutputStream createOutputStream(String filename) throws FileNotFoundException {
        return new FileOutputStream(new File(testDirectory + filename));
    }
}

