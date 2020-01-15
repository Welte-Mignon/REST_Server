import chat.communication.User;
import chat.dtos.MessageDto;
import chat.dtos.MessagesDto;
import chat.dtos.UserDto;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MessagesHandlerTest extends HandlerTest {

    @Test
    void userSendMessageWithNullToken() throws IOException {
        HttpPost messageReq = new HttpPost("http://localhost:8080/messages");
        messageReq.addHeader("Authorization", null);
        HttpResponse byeResult = httpClient.execute(messageReq);
        messageReq.abort();

        assertThat(byeResult.getStatusLine().getStatusCode(), equalTo(401));
    }

    @Test
    void userSendMessageWithWrongToken() throws IOException {
        HttpPost messageReq = new HttpPost("http://localhost:8080/messages");
        messageReq.addHeader("Authorization", UUID.randomUUID().toString());
        HttpResponse messageResult = httpClient.execute(messageReq);
        messageReq.abort();

        assertThat(messageResult.getStatusLine().getStatusCode(), equalTo(403));
    }

    @Test
    void userSendMessageWithRightToken() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));

        HttpResponse result = httpClient.execute(request);
        User user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        HttpPost messageReq = new HttpPost("http://localhost:8080/messages");
        messageReq.setEntity(new StringEntity(new Gson().toJson(new MessageDto("Hello Dolly!"))));
        messageReq.addHeader("Authorization", user.getToken().toString());
        HttpResponse messageResult = httpClient.execute(messageReq);
        MessageDto message = gson.fromJson(EntityUtils.toString(
                messageResult.getEntity(), "UTF-8"), MessageDto.class);
        messageReq.abort();

        assertThat(message.getMessage(), equalTo("Hello Dolly!"));
    }

    @Test
    void userRequestMessageMap() throws IOException {
        HttpPost request = (HttpPost)getRequest();
        request.setEntity(new StringEntity(gson.toJson(new UserDto("Nick"))));

        HttpResponse result = httpClient.execute(request);
        User user = gson.fromJson(EntityUtils.toString(result.getEntity(), "UTF-8"), User.class);

        HttpPost messagePost = new HttpPost("http://localhost:8080/messages");
        messagePost.setEntity(new StringEntity(new Gson().toJson(new MessageDto("Hello Dolly!"))));
        messagePost.addHeader("Authorization", user.getToken().toString());
        httpClient.execute(messagePost);
        messagePost.abort();

        HttpGet messageGet = new HttpGet("http://localhost:8080/messages?offset=0&count=10");
        messageGet.addHeader("Authorization", user.getToken().toString());
        HttpResponse messagesResult = httpClient.execute(messageGet);
        MessagesDto messages = gson.fromJson(EntityUtils.toString(
                messagesResult.getEntity(), "UTF-8"), MessagesDto.class);
        messageGet.abort();


        assertThat(gson.fromJson(messages.getMessages().get(0).
                getMessage(), User.class).getId(), equalTo(0));
        assertThat(gson.fromJson(messages.getMessages().get(0).
                getMessage(), User.class).getName(), equalTo("Nick"));
        assertThat(gson.fromJson(messages.getMessages().get(0).
                getMessage(), User.class).getIdleTime(), equalTo(0));
        assertThat(gson.fromJson(messages.getMessages().get(0).
                getMessage(), User.class).getToken(), equalTo(user.getToken()));
        assertThat(gson.fromJson(messages.getMessages().get(0).
                getMessage(), User.class).isOnline(), equalTo(true));
        assertThat(messages.getMessages().get(1).getMessage(), equalTo("Hello Dolly!"));
    }

    @Override
    protected String getEndpoint() {
        return "http://localhost:8080/login";
    }

}
