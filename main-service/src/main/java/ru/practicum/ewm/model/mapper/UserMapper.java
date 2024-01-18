package ru.practicum.ewm.model.mapper;

import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.dto.UserDto;
import ru.practicum.ewm.model.dto.UserShortDto;

public class UserMapper {
    public static User toUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }

    public static UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
