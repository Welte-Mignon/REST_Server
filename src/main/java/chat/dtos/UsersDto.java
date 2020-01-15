package chat.dtos;

import chat.communication.User;

import java.util.List;

public class UsersDto {
    private List<User> users;

    public UsersDto(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

}
