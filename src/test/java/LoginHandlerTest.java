import chat.dtos.UserDto;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.*;
import chat.communication.User;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import java.io.IOException;

class LoginHandlerTest extends HandlerTest {

    @Test
    void whenUserComeBack_thenAccepted() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));

        HttpResponse result = httpClient.execute(request);
        User user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        HttpPost logoutReq = new HttpPost("http://localhost:8080/logout");
        logoutReq.addHeader("Authorization", user.getToken().toString());
        httpClient.execute(logoutReq);
        logoutReq.abort();

        result = httpClient.execute(request);
        user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        assertThat(user.getId(), equalTo(0));
        assertThat(user.getName(), equalTo("Nick"));
        assertThat(user.isOnline(), equalTo(true));
        assertThat(user.getIdleTime(), equalTo(0));
    }

    @Test
    void whenUserDtoIsEmpty_thenRejected() throws IOException {
        HttpPost request = (HttpPost)getRequest();

        request.setEntity(new StringEntity(gson.toJson(null)));

        HttpResponse result = httpClient.execute(request);

        assertThat(result.getStatusLine().getStatusCode(), equalTo(401));
    }

    @Test
    void whenAlreadyOnline_thenRejected() throws IOException {
        HttpPost request = (HttpPost)getRequest();

        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));
        httpClient.execute(request);

        HttpResponse result = httpClient.execute(request);

        assertThat(result.getStatusLine().getStatusCode(), equalTo(401));
    }

    @Test
    void whenUsernameProvided_thenAccepted() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));

        HttpResponse result = httpClient.execute(request);
        User user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        assertThat(user.getId(), equalTo(0));
        assertThat(user.getName(), equalTo("Nick"));
        assertThat(user.isOnline(), equalTo(true));
        assertThat(user.getIdleTime(), equalTo(0));
    }

    @Override
    protected String getEndpoint() {
        return "http://localhost:8080/login";
    }

}