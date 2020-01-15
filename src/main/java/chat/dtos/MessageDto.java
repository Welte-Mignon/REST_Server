package chat.dtos;

public class MessageDto {
    public String message;

    public MessageDto(String message)
    {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
