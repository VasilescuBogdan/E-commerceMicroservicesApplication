package com.bogdan.user.services;

import com.bogdan.user.controllers.models.GetUser;
import com.bogdan.user.controllers.models.UpdateUser;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.entities.enums.Role;
import com.bogdan.user.persistence.repositories.UserRepository;
import com.bogdan.user.service.impl.UserServiceImpl;
import com.bogdan.user.utils.exceptions.UserNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void getAllUsers_serviceReturnsAllUsers_returnUsers() {
        //Arrange
        User user1 = User.builder()
                         .id(1L)
                         .username("user1")
                         .password("password1")
                         .role(Role.USER)
                         .build();
        User user2 = User.builder()
                         .id(2L)
                         .username("user2")
                         .password("password2")
                         .role(Role.USER)
                         .build();
        Mockito.when(repository.findAll())
               .thenReturn(List.of(user1, user2));

        //Act
        List<GetUser> actualUsers = service.getAllUsers();

        //Assert
        Assertions.assertThat(actualUsers)
                  .isNotNull()
                  .hasSize(2);
        Assertions.assertThat(actualUsers.get(0))
                  .isEqualTo(mapUserToGetUser(user1));
        Assertions.assertThat(actualUsers.get(1))
                  .isEqualTo(mapUserToGetUser(user2));
    }

    @Test
    void getUser_whenRepositoryReturnsPresentUser_returnUser() {
        //Arrange
        long userId = 1L;
        User user = new User(userId, "user", "password", Role.USER);
        Mockito.when(repository.findById(userId))
               .thenReturn(Optional.of(user));

        //Act
        GetUser actualUser = service.getUser(userId);

        //Assert
        Assertions.assertThat(actualUser)
                  .isNotNull()
                  .isEqualTo(mapUserToGetUser(user));
    }

    @Test
    void getUser_whenRepositoryReturnsNothing_throwResourceNotFoundException() {
        //Arrange
        long userId = 99L;
        Mockito.when(repository.findById(userId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                  .isThrownBy(() -> {
                      //Act
                      service.getUser(userId);
                  })
                  .withMessage("There is no user with id " + userId);
    }

    @Test
    void deleteUser_deleteMethodIsCalled() {
        //Arrange
        long userId = 1L;

        //Act
        service.deleteUser(userId);

        //Assert
        verify(repository, times(1)).deleteById(userId);
    }

    @Test
    void updateUser_whenRepositoryReturnsActualUser_saveMethodIsCalled() {
        //Arrange
        long userId = 1L;
        UpdateUser updateUser = UpdateUser.builder()
                                          .username("new username")
                                          .password("new password")
                                          .build();
        when(encoder.encode(updateUser.password())).thenReturn("new encoded password");
        User user = new User(userId, "username", "password", Role.USER);
        Mockito.when(repository.findById(userId))
               .thenReturn(Optional.of(user));

        //Act
        service.updateUser(userId, updateUser);

        //Assert
        verify(repository, times(1)).save(
                new User(userId, updateUser.username(), "new encoded password", user.getRole()));
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
        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                  .isThrownBy(() -> {
                      //Act
                      service.updateUser(userId, user);
                  })
                  .withMessage("There is no user with id " + userId);
    }

    private GetUser mapUserToGetUser(User user) {
        return GetUser.builder()
                      .username(user.getUsername())
                      .password(user.getPassword())
                      .role(user.getRole())
                      .build();
    }
}
