package com.bogdan.user.services;

import com.bogdan.user.controllers.models.GetUser;
import com.bogdan.user.controllers.models.UpdateUser;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.entities.enums.Role;
import com.bogdan.user.persistence.repositories.UserRepository;
import com.bogdan.user.service.impl.UserServiceImpl;
import com.bogdan.user.utils.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @InjectMocks
    UserServiceImpl service;

    @Test
    void getAllUsers() {
        //Arrange
        User user1 = User.builder()
                         .id(1L)
                         .username("user1")
                         .password("password1")
                         .role(Role.USER)
                         .build();
        User user2 = User.builder()
                         .id(1L)
                         .username("user1")
                         .password("password1")
                         .role(Role.USER)
                         .build();
        Mockito.when(repository.findAll())
               .thenReturn(List.of(user1, user2));

        //Act
        List<GetUser> actualUsers = service.getAllUsers();

        //Assert
        Assertions.assertNotNull(actualUsers);
        Assertions.assertEquals(2, actualUsers.size());
        Assertions.assertEquals(user1.getUsername(), actualUsers.get(0)
                                                                .username());
        Assertions.assertEquals(user1.getPassword(), actualUsers.get(0)
                                                                .password());
        Assertions.assertEquals(user1.getRole(), actualUsers.get(0)
                                                            .role());
        Assertions.assertEquals(user2.getUsername(), actualUsers.get(1)
                                                                .username());
        Assertions.assertEquals(user2.getPassword(), actualUsers.get(1)
                                                                .password());
        Assertions.assertEquals(user2.getRole(), actualUsers.get(1)
                                                            .role());
    }

    @Test
    void getUser_whenRepositoryReturnsPresentUser_returnUser() {
        //Arrange
        long userId = 1L;
        User user = User.builder()
                        .id(userId)
                        .username("user")
                        .password("password")
                        .build();
        Mockito.when(repository.findById(userId))
               .thenReturn(Optional.of(user));

        //Act
        GetUser actualUser = service.getUser(userId);

        //Assert
        Assertions.assertNotNull(actualUser);
        Assertions.assertEquals(user.getUsername(), actualUser.username());
        Assertions.assertEquals(user.getPassword(), actualUser.password());
        Assertions.assertEquals(user.getRole(), actualUser.role());
    }

    @Test
    void getUser_whenRepositoryReturnsEmptyOptional_throwResourceNotFoundException() {
        //Arrange
        long userId = 99L;
        Mockito.when(repository.findById(userId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThrows(UserNotFoundException.class, () -> {
            //Act
            service.getUser(userId);
        });
    }

    @Test
    void updateUser_whenRepositoryReturnsEmptyOptional_throwResourceNotFoundException() {
        //Arrange
        long userId = 99L;
        UpdateUser user = UpdateUser.builder()
                                    .username("username")
                                    .password("password")
                                    .build();
        Mockito.when(repository.findById(userId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThrows(UserNotFoundException.class, () -> {
            //Act
            service.updateUser(userId, user);
        });
    }
}
