package com.bogdan.user.service;

import com.bogdan.user.controllers.models.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUser(long id);

    void deleteUser(long id);

    void updateUser(long id, UserDto updatedUser);
}
