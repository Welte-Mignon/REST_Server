import chat.communication.Message;
import chat.communication.User;
import chat.dtos.UserDto;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LogoutHandlerTest extends HandlerTest {

    @Test
    void checkLogoutUser() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));

        HttpResponse result = httpClient.execute(request);
        User user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        HttpPost logoutReq = new HttpPost("http://localhost:8080/logout");
        logoutReq.addHeader("Authorization", user.getToken().toString());
        HttpResponse byeResult = httpClient.execute(logoutReq);
        Message message = gson.fromJson(EntityUtils.toString(byeResult.getEntity(), "UTF-8"), Message.class);
        logoutReq.abort();

        assertThat(message.getMessage(), equalTo("bye!"));
    }

    @Test
    void checkLogoutUserWithNullToken() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));
        httpClient.execute(request);

        HttpPost logoutReq = new HttpPost("http://localhost:8080/logout");
        logoutReq.addHeader("Authorization", null);
        HttpResponse byeResult = httpClient.execute(logoutReq);
        logoutReq.abort();

        assertThat(byeResult.getStatusLine().getStatusCode(), equalTo(401));
    }

    @Override
    protected String getEndpoint() {
        return "http://localhost:8080/login";
    }

}
