package org.mystudying.booksmarket2.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mystudying.booksmarket2.domain.User;
import org.mystudying.booksmarket2.exceptions.EmailAlreadyExistsException;
import org.mystudying.booksmarket2.repositories.BookRepository;
import org.mystudying.booksmarket2.repositories.BookingRepository;
import org.mystudying.booksmarket2.repositories.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookingRepository bookingRepository;

    @BeforeEach
    void beforeEach() {
        userService = new UserService(userRepository, bookRepository, bookingRepository);
    }

    @Test
    void create() {
        User newUser = new User(1, "New User", "new@example.com");
        when(userRepository.create(argThat(u ->
                u.getName().equals(newUser.getName()) && u.getEmail().equals(newUser.getEmail())))).thenReturn(1L);
        long id = userService.create("New User", "new@example.com");
        assertThat(id).isEqualTo(1L);
        verify(userRepository).create(argThat(u ->
                u.getName().equals(newUser.getName()) && u.getEmail().equals(newUser.getEmail())));
    }

    @Test
    void createThrowsExceptionIfDuplicateKey() {
        when(userRepository.create(any(User.class))).thenThrow(new org.springframework.dao.DuplicateKeyException(""));
        assertThatExceptionOfType(EmailAlreadyExistsException.class)
                .isThrownBy(() -> userService.create("New User", "new@example.com"));
        verify(userRepository).create(any(User.class));
    }
}
