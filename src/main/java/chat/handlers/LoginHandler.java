package chat.handlers;

import com.google.gson.Gson;
import chat.dtos.UserDto;
import io.undertow.server.HttpHandler;
import chat.communication.Message;
import chat.communication.User;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpServerExchange;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.undertow.util.Headers.WWW_AUTHENTICATE;

/**
 * @author Baturin Evgeniy
 * @version 0.0.1
 * Command handler used for authentication
 * Maps requests from /login endpoint
 */
public class LoginHandler implements HttpHandler {
    private static final int SERVER_ID = -1;
    private static final int LOGINNING = -1;
    private final Map<String, User> nameUsers;
    private final Map<UUID, User> tokenUsers;
    private final List<Message> messages;
    private final Gson gson = new Gson();

    /**
     * @param nameUsers Mapping between names and user accounts
     * @param tokenUsers Mapping between auth tokens and user accounts
     * @param messages List of messages
     */
    public LoginHandler(Map<String, User> nameUsers, Map<UUID, User> tokenUsers, List<Message>  messages) {
        super();
        this.nameUsers = nameUsers;
        this.tokenUsers = tokenUsers;
        this.messages = messages;
    }

    /**
     * Is called when a yet unregistered user comes
     * @param userName Name of the new user
     */
    private void handleUserCreation(final String userName,
                                    final HttpServerExchange exchange) {
        UUID userToken = UUID.randomUUID();
        User newUser = new User(nameUsers.size(), userName, true, userToken);

        tokenUsers.put(userToken, newUser);
        nameUsers.put(userName, newUser);
        messages.add(new Message(LOGINNING, /* The notification about LOGIN for another users */
                gson.toJson(tokenUsers.get(userToken)), SERVER_ID));
        exchange.getResponseSender().send( /* Send an instance of the user */
                gson.toJson(newUser), IoCallback.END_EXCHANGE);
    }

    /**
     * Is called when this user already exists and is online,
     * therefore restricting access
     */
    private void handleDenyBecauseAlreadyOnline(final HttpServerExchange exchange) {
        exchange.setStatusCode(401);
        exchange.getResponseHeaders().put(WWW_AUTHENTICATE,
                "Token realm='Username is already in use'");
    }

    /**
     * Is called when this user already exists and is offline,
     * therefore setting their status to online and giving them
     * a new auth token
     */
    private void handleUserComeback(final String userName,
                                    final HttpServerExchange exchange) {
        UUID userToken = UUID.randomUUID();

        nameUsers.get(userName).toOnline();
        nameUsers.get(userName).toZeroIdleTime();
        nameUsers.get(userName).setToken(userToken);
        tokenUsers.put(userToken, nameUsers.get(userName));

        messages.add(new Message(LOGINNING, /* The notification about LOGIN for another users */
                gson.toJson(tokenUsers.get(userToken)), SERVER_ID));
        exchange.getResponseSender().send( /* Send an instance of the user */
                gson.toJson(nameUsers.get(userName)), IoCallback.END_EXCHANGE);
    }

    /**
     * This method processes request that matched the /login endpoint
     * /login [with username as JSON body] -> register new user \
     * OR update online status of the existing one, and provide token.
     * Appropriate HTTP error codes are set in response message in case errors occur.
     * @param exchange Object used both for reading/handling request and writing/sending response
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((HttpServerExchange excng, String bytes) -> {
            UserDto userDto = gson.fromJson(bytes, UserDto.class);

            if (userDto == null) {
                handleDenyBecauseAlreadyOnline(exchange);
            } else if (!nameUsers.containsKey(userDto.getUsername())) {
                handleUserCreation(userDto.getUsername(), exchange);
            } else if (nameUsers.get(userDto.getUsername()).isOnline()) {
                handleDenyBecauseAlreadyOnline(exchange);
            } else {
                handleUserComeback(userDto.getUsername(), exchange);
            }
        });
    }

}
