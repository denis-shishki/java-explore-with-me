package ru.practicum.ewm.service;

import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto postUser(UserDto userDto);

    void deleteUser(long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    User checkExistUser(long userId);
}
