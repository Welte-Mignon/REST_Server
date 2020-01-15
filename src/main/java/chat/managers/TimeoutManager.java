package chat.managers;

import com.google.gson.Gson;
import chat.communication.Message;
import chat.communication.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Baturin Evgeniy
 * @version 0.0.1
 * The class is responsible for expelling inactive users from the chat.
 * It acts as a separate thread
 */
public class TimeoutManager extends Thread {
    public static final int SLEEP_DURATION = 5000;
    public static final int MAX_ALLOWED_IDLE_TIME = 30;
    private final Map<UUID, User> tokenUsers;
    private final Map<String, User> nameUsers;
    private final List<Message> messages;
    private final int SERVER_ID = -1;
    private final int KICKING = -4;

    /**
     * @param nameUsers Mapping from names to user accounts
     * @param tokenUsers Mapping from auth tokens to user accounts
     * @param messages List of messages
     */
    public TimeoutManager(final Map<String, User> nameUsers,
                          final Map<UUID, User> tokenUsers,
                          final List<Message> messages) {
        this.tokenUsers = tokenUsers;
        this.nameUsers = nameUsers;
        this.messages = messages;
    }


    /**
     * Expel inactive user from the chat
     * @param userToken Authorisation token of the user to kick
     */
    private void kick(final UUID userToken) {
        nameUsers.get(tokenUsers.get(userToken).getName()).toOffline();
        messages.add(new Message(KICKING,
                (new Gson()).toJson(tokenUsers.get(userToken)), SERVER_ID));

        tokenUsers.entrySet().removeIf(entry -> entry.getKey().equals(userToken));
    }


    /**
     * Continuously check for inactive users and kick them
     */
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(SLEEP_DURATION);
                for (UUID token: tokenUsers.keySet()) {
                    tokenUsers.get(token).increaseIdleTime();
                    if (tokenUsers.get(token).getIdleTime() > MAX_ALLOWED_IDLE_TIME) {
                        kick(token);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
