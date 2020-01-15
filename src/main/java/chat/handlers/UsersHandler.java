package chat.handlers;

import com.google.gson.Gson;
import chat.dtos.UsersDto;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import chat.communication.User;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * @author Baturin Evgeniy
 * @version 0.0.1
 * Command handler used for retrieving information about users
 * Maps requests from /users and /users/(id) endpoints
 */
public class UsersHandler implements HttpHandler {
    private final Map<String, User> nameUsers;
    private final Map<UUID, User> tokenUsers;

    /**
     * @param nameUsers Mapping between names and user accounts
     * @param tokenUsers Mapping between auth tokens and user accounts
     */
    public UsersHandler(final Map<String, User> nameUsers, final Map<UUID, User> tokenUsers) {
        super();
        this.nameUsers = nameUsers;
        this.tokenUsers = tokenUsers;
    }

    /**
     * Ensures that the user is sending existing auth token in his request
     * @param exchange Object used both for reading/handling request and writing/sending response
     * @throws IllegalAccessException In case no token was provided
     * @throws IllegalArgumentException In case invalid token was provided
     */
    private void checkAuthorization(HttpServerExchange exchange)
            throws IllegalAccessException, IllegalArgumentException {
        UUID userToken = getUserToken(exchange);

        tokenUsers.get(userToken).getId();
    }

    /**
     * Retrieves auth token (UUID) from user's request
     * @return Token, if it was specified by user
     * @throws IllegalAccessException In case no token was provided
     */
    private UUID getUserToken(HttpServerExchange exchange) throws IllegalAccessException {
        String strUserToken = exchange.getRequestHeaders().get("Authorization").get(0);

        if (strUserToken.isEmpty()) throw new IllegalAccessException();

        return UUID.fromString(strUserToken);
    }

    /**
     * Tries to retrieve desirable user's ID.
     * If successful, sends information about them in response.
     * @throws NoSuchFieldError If ID was not specified
     */
    private void sendUserInfo(HttpServerExchange exchange) throws NoSuchFieldError {
        int id = Integer.parseInt(exchange.getRelativePath().substring(1)); /* Getting a user's number */

        if (getById(id) == null) {
            throw new NoSuchFieldError();
        } else {
            exchange.getResponseSender().send((new Gson()).toJson(getById(id)), IoCallback.END_EXCHANGE);
        }

    }

    /**
     * Searches for user in local storage by the ID
     * @param id ID of the user
     * @return
     */
    private User getById(final int id)
    {
        for (User user : nameUsers.values()) {
            if (user.getId() == id)   {
                return user;
            }
        }

        return null;
    }

    /**
     * This method processes request that matched the /users endpoint
     * /users -> list of all users
     * /users/(id) -> info about specific user
     * Appropriate HTTP error codes are set in response message in case errors occur.
     * @param exchange Object used both for reading/handling request and writing/sending response
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        try {
            checkAuthorization(exchange);
            if (exchange.getRelativePath().isEmpty()) {
                exchange.getResponseSender().send((new Gson()). /* Send a list with online users */
                        toJson(new UsersDto(new ArrayList<>(nameUsers.values()))), IoCallback.END_EXCHANGE);
            } else { /* Send info about specific user */
                sendUserInfo(exchange);
            }
        } catch (IllegalArgumentException | NullPointerException uncorrTokExc)   {
            exchange.setStatusCode(403);
        } catch (IllegalAccessException emptyTokenException)  {
            exchange.setStatusCode(401);
        } catch (NoSuchFieldError unknownUser) {
            exchange.setStatusCode(404);
        }
    }

}
