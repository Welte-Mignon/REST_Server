package chat.handlers;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import chat.dtos.MessageDto;
import chat.dtos.MessagesDto;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import chat.communication.Message;
import chat.communication.User;

import java.util.*;

public class MessagesHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private Map<UUID, User> tokenUsers;
    private List<Message> messages;

    public MessagesHandler(final Map tokenUsers, List messages) {
        super();
        this.tokenUsers = tokenUsers;
        this.messages = messages;
    }

    private void treatNewMessage(HttpServerExchange exchange, final String bytes)
            throws IllegalAccessException, IllegalArgumentException, NullPointerException {
        UUID userToken = getUserToken(exchange);
        MessageDto content = gson.fromJson(bytes, MessageDto.class);

        messageSaving(tokenUsers.get(userToken), content.message, exchange);
        System.out.println(content.message);
    }

    private UUID getUserToken(HttpServerExchange exchange) throws IllegalAccessException {
        String strUserToken = exchange.getRequestHeaders().get("Authorization").get(0);

        if (strUserToken.isEmpty()) throw new IllegalAccessException();

        return UUID.fromString(strUserToken);
    }

    private void messageSaving(final User user, final String message,
                               final HttpServerExchange exchange) {
        Message newMessage = new Message(messages.size(), message, user.getId());

        messages.add(newMessage);
        exchange.getResponseSender().send(gson.toJson(newMessage), IoCallback.END_EXCHANGE);
    }

    private void sendMessagesMap (final Map<String, String> offsetCount,
                                  HttpServerExchange exchange) {
        ArrayList<Message> requiredMessages = new ArrayList<>();
        int count = Integer.parseInt(offsetCount.get("count"));
        int offset = Integer.parseInt(offsetCount.get("offset"));

        for (int i = 0; i < count && offset + i < messages.size(); i++) {
            requiredMessages.add(messages.get(offset + i));
        }

        exchange.getResponseSender().send(gson./* Send a list with required messages */
                toJson(new MessagesDto(requiredMessages)), IoCallback.END_EXCHANGE);
    }

    private Map<String, String> getOffsetCount (HttpServerExchange exchange) {
        Map<String, String> offsetCount = new HashMap<>();

        offsetCount.put("offset", "0");/* By default */
        offsetCount.put("count", "10");

        offsetCount.putAll(Splitter.on("&")./* Parsing of input data */
                withKeyValueSeparator("=").split(exchange.getQueryString()));

        return offsetCount;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((HttpServerExchange excng, String bytes) -> {
            try {
                if (exchange.getQueryString().isEmpty()) { /* "Offset-count string" is empty */
                    treatNewMessage(exchange, bytes);
                } else {
                    sendMessagesMap(getOffsetCount(exchange), exchange);
                }
            } catch (IllegalArgumentException | NullPointerException uncorrTokExc) {
                exchange.setStatusCode(403);
            } catch (IllegalAccessException emptyTokenException) {
                exchange.setStatusCode(401);
            }
        });
    }

}
