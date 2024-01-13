package ru.practicum.service.model.mapper;

import ru.practicum.service.model.User;
import ru.practicum.service.model.dto.UserDto;

public class UserMapper {
    public static User toUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
