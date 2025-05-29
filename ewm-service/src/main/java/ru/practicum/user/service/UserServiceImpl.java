package ru.practicum.user.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserJPARepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserJPARepository userJPARepository;

    @Autowired
    public UserServiceImpl(UserJPARepository userJPARepository) {
        this.userJPARepository = userJPARepository;
    }

    @Transactional
    public UserDto saveUser(NewUserRequest newUserRequestO) {
        User user = UserMapper.toModel(newUserRequestO);
        user = userJPARepository.save(user);
        return UserMapper.toFullDto(user);
    }

    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        List<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userJPARepository.findAllByIds(ids);
        }  else if (from != null && size != null) {
            users = userJPARepository.findAllWithPagination(from, size);
        } else {
            users = userJPARepository.findAll();
        }
        return users.stream()
                .map(UserMapper::toFullDto)
                .collect(Collectors.toList());
     }

    @Transactional
    public void remove(Long id) {
        if (!userJPARepository.existsById(id)) {
            throw new NotFoundException("User with id = " + id + " not found.");
        }
        userJPARepository.deleteById(id);
    }
}