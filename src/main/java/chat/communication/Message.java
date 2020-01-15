package chat.communication;

/**
 * @author Baturin Evgeniy
 * @version 0.0.1
 * An entity class that represents user's message in the chat.
 */
public class Message {
    private final int id;
    private final String message;
    private final int author;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message1 = (Message) o;

        if (id != message1.id) return false;
        if (author != message1.author) return false;
        return message != null ? message.equals(message1.message) : message1.message == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + author;

        return result;
    }

    /**
     * Constructor of message entity class.
     * @param id Unique ID of the message
     * @param message Message content
     * @param author ID of the message's author; see {@Link User}
     */
    public Message(final int id, final String message, final int author) {
        this.id = id;
        this.message = message;
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public int getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }
}