package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.UserCreateDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    @Transactional
    public UserDto create(UserCreateDto dto) {
        User user = UserMapper.toEntity(dto);

        String email = user.getEmail();
        if (email == null || email.isBlank() || email.length() < 6 || email.length() > 254 || !email.contains("@")) {
            log.warn("Invalid email: must contain '@' and be 6..254 chars: {}", email);
            throw new ValidationException("Invalid email: must contain '@' and be 6..254 chars");
        }
        String[] parts = email.split("@", -1);

        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            log.warn("Invalid email format");
            throw new ValidationException("Invalid email format");
        }

        String[] labels = parts[1].split("\\.");
        for (String label : labels) {
            if (label.isEmpty() || label.length() > 63) {
                log.warn("Each domain label length must be 1..63 characters");
                throw new ValidationException("Each domain label length must be 1..63 characters");
            }
        }


        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            log.warn("Email already exists: {}", user.getEmail());
            throw new ConflictException("Email already exists: " + user.getEmail());
        }

        User created = userRepository.save(user);
        log.info("New user created: ID={} email={}", created.getId(), created.getEmail());

        return UserMapper.toDto(created);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (userId == null) {
            log.warn("User id must not be null");
            throw new ValidationException("User id must not be null");
        }

        if (!userRepository.existsById(userId)) {
            log.warn("User with id {} not found", userId);
            throw new NotFoundException("User not found: id=" + userId);
        }

        userRepository.deleteById(userId);
        log.info("User deleted: id={}", userId);
    }

    @Override
    public List<UserDto> findAll(List<Long> ids, int from, int size) {
        if (from < 0 || size <= 0) {
            log.error("Invalid pagination params: from={}, size={}", from, size);
            throw new BadRequestException("Invalid pagination params: from >= 0 and size > 0 are required");
        }

        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<User> content;
        if (ids == null || ids.isEmpty()) {
            log.debug("Fetching users page: from={}, size={}", from, size);
            content = userRepository.findAll(page).getContent();
        } else {
            log.debug("Fetching users by ids: {}, from={}, size={}", ids, from, size);
            content = userRepository.findAllByIdIn(ids, page).getContent();
        }

        return content.stream()
                .map(UserMapper::toDto)
                .toList();
    }


}
