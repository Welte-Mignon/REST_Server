import chat.communication.User;
import chat.dtos.UserDto;
import chat.dtos.UsersDto;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UsersHandlerTest extends HandlerTest {

    @Test
    void whenUserWithWrongToken() throws IOException {
        HttpPost usersReq = new HttpPost("http://localhost:8080/users");
        usersReq.addHeader("Authorization", UUID.randomUUID().toString());
        HttpResponse usersResult = httpClient.execute(usersReq);
        usersReq.abort();

        assertThat(usersResult.getStatusLine().getStatusCode(), equalTo(403));
    }

    @Test
    void whenUserWithNullToken() throws IOException {
        HttpPost usersReq = new HttpPost("http://localhost:8080/users");
        usersReq.addHeader("Authorization", null);
        HttpResponse usersResult = httpClient.execute(usersReq);
        usersReq.abort();

        assertThat(usersResult.getStatusLine().getStatusCode(), equalTo(401));
    }

    @Test
    void whenUserRequestInfoAboutAnotherUser() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));

        HttpResponse result = httpClient.execute(request);
        User user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        HttpPost usersReq = new HttpPost("http://localhost:8080/users/0");
        usersReq.addHeader("Authorization", user.getToken().toString());
        User userInfo = gson.fromJson(EntityUtils.toString(
                httpClient.execute(usersReq).getEntity(), "UTF-8"), User.class);
        usersReq.abort();

        assertThat(userInfo.getId(), equalTo(0));
        assertThat(userInfo.getName(), equalTo("Nick"));
        assertThat(userInfo.isOnline(), equalTo(true));
        assertThat(userInfo.getIdleTime(), equalTo(0));
    }

    @Test
    void whenUserRequestInfoAboutNonexistentUser() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));

        HttpResponse result = httpClient.execute(request);
        User user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        HttpPost usersReq = new HttpPost("http://localhost:8080/users/1");
        usersReq.addHeader("Authorization", user.getToken().toString());
        HttpResponse usersResult = httpClient.execute(usersReq);
        usersReq.abort();

        assertThat(usersResult.getStatusLine().getStatusCode(), equalTo(404));
    }

    @Test
    void whenUserRequestListOfUsers() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));

        HttpResponse result = httpClient.execute(request);
        User user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        HttpPost usersReq = new HttpPost("http://localhost:8080/users");
        usersReq.addHeader("Authorization", user.getToken().toString());
        UsersDto users = gson.fromJson(EntityUtils.toString(
                httpClient.execute(usersReq).getEntity(), "UTF-8"), UsersDto.class);
        usersReq.abort();

        assertThat(users.getUsers().size(), equalTo(1));
        assertThat(users.getUsers().get(0).getId(), equalTo(0));
        assertThat(users.getUsers().get(0).getName(), equalTo("Nick"));
        assertThat(users.getUsers().get(0).isOnline(), equalTo(true));
        assertThat(users.getUsers().get(0).getIdleTime(), equalTo(0));
    }

    @Override
    protected String getEndpoint() {
        return "http://localhost:8080/login";
    }

}
