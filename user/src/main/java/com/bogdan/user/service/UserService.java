package com.bogdan.user.service;

import com.bogdan.user.controllers.models.GetUser;
import com.bogdan.user.controllers.models.UpdateUser;

import java.util.List;

public interface UserService {

    List<GetUser> getAllUsers();

    GetUser getUser(long id);

    void deleteUser(long id);

    void updateUser(long id, UpdateUser updatedUser);

}
