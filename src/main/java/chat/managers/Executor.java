package chat.managers;

import chat.handlers.*;
import io.undertow.Handlers;
import io.undertow.Undertow;
import chat.communication.Message;
import chat.communication.User;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class Executor implements Closeable {
    private final Map<String, User> nameUsers;
    private final Map<UUID, User> tokenUsers;
    private final List<Message> messages;
    private final TimeoutManager timeoutManager;
    private Undertow server;

    {
        messages = new ArrayList<>();
        tokenUsers = new ConcurrentHashMap<>();
        nameUsers = new ConcurrentHashMap<>();
        timeoutManager = new TimeoutManager(getNameUsers(), getTokenUsers(), getMessages());
    }


    public void configure() {
        server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(
                        Handlers.path()
                                .addPrefixPath("/login", new LoginHandler(getNameUsers(), getTokenUsers(), getMessages()))
                                .addPrefixPath("/logout", new LogoutHandler(getNameUsers(), getTokenUsers(), getMessages()))
                                .addPrefixPath("/messages", new MessagesHandler(getTokenUsers(), getMessages()))
                                .addPrefixPath("/users", new UsersHandler(getNameUsers(), getTokenUsers()))
                ).build();
    }

    public void run() {
        getServer().start();
        timeoutManager.start();
    }

    public Map<String, User> getNameUsers() {
        return nameUsers;
    }

    public Map<UUID, User> getTokenUsers() {
        return tokenUsers;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Undertow getServer() {
        return server;
    }

    @Override
    public void close() {
        server.stop();
    }
}
