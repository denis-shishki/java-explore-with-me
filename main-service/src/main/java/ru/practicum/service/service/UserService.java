package ru.practicum.service.service;

import org.springframework.stereotype.Service;
import ru.practicum.service.model.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto postUser(UserDto userDto);
    void deleteUser(long userId);
    List<UserDto> getUsers(List<Long> ids, int from, int size);
}
