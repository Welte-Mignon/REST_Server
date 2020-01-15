package chat.handlers;

import com.google.gson.Gson;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import chat.communication.Message;
import chat.communication.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Baturin Evgeniy
 * @version 0.0.1
 * Command handler used for exiting the service
 * Maps requests from /logout endpoint
 */
public class LogoutHandler implements HttpHandler {
    private final Gson gson = (new Gson());
    private Map<String, User> nameUsers;
    private Map<UUID, User> tokenUsers;
    private List<Message> messages;
    private static final int SERVER_ID = -1;
    private static final int LOGOUTING = -2;
    private final int BYE_MESSAGE = -3;

    /**
     * @param nameUsers Mapping between names and user accounts
     * @param tokenUsers Mapping between auth tokens and user accounts
     */
    public LogoutHandler(Map<String, User> nameUsers,
                         Map<UUID, User> tokenUsers,
                         List<Message> messages) {
        super();
        this.nameUsers = nameUsers;
        this.tokenUsers = tokenUsers;
        this.messages = messages;
    }

    /*
     * Retrieves passed token from "Authorization" header
     * */
    private UUID getUserToken(HttpServerExchange exchange) throws IllegalAccessException {
        String strUserToken = exchange.getRequestHeaders().get("Authorization").get(0);

        if (strUserToken.isEmpty()) throw new IllegalAccessException();

        return UUID.fromString(strUserToken);
    }

    /*
     * Track user leaving:
     * 1) Make them offline.
     * 2) Make their token null.
     * 3) Remove all info about them from the system.
     * */
    private void trackUserLeft(final UUID userToken)
            throws IllegalArgumentException, NullPointerException {
        nameUsers.get(tokenUsers.get(userToken).getName()).toOffline();
        nameUsers.get(tokenUsers.get(userToken).getName()).setToken(null);

        messages.add(new Message(LOGOUTING, /* The notification about LOGOUT for another users */
                gson.toJson(tokenUsers.get(userToken)), SERVER_ID));
        tokenUsers.entrySet().removeIf(entry -> entry.getKey().equals(userToken));
    }

    /*
     * This method processes request that matched the /logout endpoint
     * /logout [with token in headers] -> say "bye" to user and wipe all info about them
     * */
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        try {
            UUID userToken = getUserToken(exchange);

            trackUserLeft(userToken);
            exchange.getResponseSender().send(gson.toJson(new Message(BYE_MESSAGE,
                    "bye!", SERVER_ID)), IoCallback.END_EXCHANGE);
        } catch (IllegalArgumentException | NullPointerException uncorrTokExc) {
            exchange.setStatusCode(403);
        } catch (IllegalAccessException emptyTokenException) {
            exchange.setStatusCode(401);
        }
    }

}
