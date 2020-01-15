package chat.dtos;

public class UserDto {
    public UserDto(String username) {
        this.username = username;
    }

    private String username;

    public String getUsername() {
        return username;
    }

}
