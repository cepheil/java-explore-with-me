package ru.practicum.user.service;

import ru.practicum.user.dto.UserCreateDto;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(UserCreateDto dto);

    void delete(Long userId);

    List<UserDto> findAll(List<Long> ids, int from, int size);

}
