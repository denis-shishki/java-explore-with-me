package ru.practicum.service.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.service.exception.NotFoundException;
import ru.practicum.service.model.User;
import ru.practicum.service.model.dto.UserDto;
import ru.practicum.service.model.mapper.UserMapper;
import ru.practicum.service.paginator.Paginator;
import ru.practicum.service.repository.UserRepository;
import ru.practicum.service.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto postUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Category with id=" + userId + " was not found");
        }
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = Paginator.getPageable(from, size);
        Page<User> users;

        if (ids == null) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findAllByIdIn(ids, pageable);
        }

        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public User checkExistUser(long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException("User with id= " + userId + " was not found");
        } else {
            return user.get();
        }
    }

}
