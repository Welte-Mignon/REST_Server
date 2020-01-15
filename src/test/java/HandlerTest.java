import com.google.gson.Gson;
import chat.managers.Executor;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public abstract class HandlerTest {
    protected static CloseableHttpClient httpClient = HttpClients.custom()
            .setRetryHandler(new DefaultHttpRequestRetryHandler(5, true))
            .build();
    protected static Gson gson = new Gson();
    private Executor executor;
    private HttpRequestBase request;

    @BeforeEach
    synchronized void setUp() {
        executor = new Executor();
        request = new HttpPost(getEndpoint());

        executor.configure();
        executor.run();
        request.addHeader("content-type", "application/json");
    }

    @AfterEach
    synchronized void tearDown() {
        if (executor != null) {
            executor.close();
            request.abort();
        }
    }

    protected abstract String getEndpoint();

    protected HttpRequestBase getRequest() {
        return request;
    }

}