package chat.dtos;

import chat.communication.Message;

import java.util.List;

public class MessagesDto {
    private List<Message> messages;

    public MessagesDto(List<Message> messages) {
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessagesDto that = (MessagesDto) o;

        return messages != null ? messages.equals(that.messages) : that.messages == null;
    }

    @Override
    public int hashCode() {
        return messages != null ? messages.hashCode() : 0;
    }

}
