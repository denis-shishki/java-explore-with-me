package ru.practicum.ewm.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.dto.UserDto;
import ru.practicum.ewm.model.mapper.UserMapper;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.UserService;

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
