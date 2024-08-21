package com.bogdan.user.service.impl;

import com.bogdan.user.controllers.models.UpdateUser;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.repositories.UserRepository;
import com.bogdan.user.service.UserService;
import com.bogdan.user.controllers.models.GetUser;
import com.bogdan.user.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public List<GetUser> getAllUsers() {
        return userRepository.findAll()
                             .stream()
                             .map(this::mapUserToUserDto)
                             .toList();
    }

    @Override
    public GetUser getUser(long id) {
        return userRepository.findById(id)
                             .map(this::mapUserToUserDto)
                             .orElseThrow(() -> new UserNotFoundException("There is no user with id " + id));
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void updateUser(long id, UpdateUser updatedUser) {
        User user = userRepository.findById(id)
                                  .orElseThrow(() -> new UserNotFoundException("There is no user with id " + id));
        user.setUsername(updatedUser.username());
        user.setPassword(passwordEncoder.encode(updatedUser.password()));
        userRepository.save(user);
    }

    private GetUser mapUserToUserDto(User user) {
        return GetUser.builder()
                      .username(user.getUsername())
                      .password(user.getPassword())
                      .role(user.getRole())
                      .build();
    }
}
