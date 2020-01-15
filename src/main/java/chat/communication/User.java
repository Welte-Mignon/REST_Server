package chat.communication;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Baturin Evgeniy
 * @version 0.0.1
 * An entity class that represents user of the system.
 */
public class User implements Serializable {
    private final int id;
    private final String name;
    private boolean online;
    private UUID token;
    private transient int idleTime;

    /**
     * Constructor of user entity class.
     * @param id A unique number representing user in messages
     * @param name Name of the user
     * @param online A flag indicating whether there is an active session of this user
     * @param token Access token generated on each visit for further authorisation
     */
    public User(final int id, final String name, final boolean online, final UUID token) {
        this.id = id;
        this.name = name;
        this.online = online;
        this.token = token;
        this.idleTime = 0;
    }

    /**
     * Provides info of how much time the user was inactive
     * @return Time passed since this user's last inactivity.
     */
    public int getIdleTime() { return this.idleTime; }

    /**
     * Sets time of user's inactivity to zero (0).
     */
    public void toZeroIdleTime() { this.idleTime = 0; }

    /**
     * Adds 5 points to user's inactivity count.
     */
    public void increaseIdleTime() { this.idleTime += 5; }

    /**
     * Sets user's new unique auth identifier.
     * @param token New token (GUID)
     */
    public void setToken(final UUID token) {
        this.token = token;
    }

    /**
     * Sets online status to true.
     */
    public void toOnline() {
        this.online = true;
    }

    /**
     * Sets online status to false.
     */
    public void toOffline() {
        this.online = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getToken() {
        return token;
    }

    public boolean isOnline() {
        return online;
    }

}