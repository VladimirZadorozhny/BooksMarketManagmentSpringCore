package org.mystudying.booksmarket2.repositories;


import org.junit.jupiter.api.Test;
import org.mystudying.booksmarket2.domain.User;
import org.mystudying.booksmarket2.exceptions.EmailAlreadyExistsException;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@JdbcTest
@Transactional
@Import(UserRepository.class)
@Sql("/insertTestRecords.sql")
class UserRepositoryTest {
    private static final String USERS_TABLE = "users";
    private final UserRepository userRepository;
    private final JdbcClient jdbcClient;

    public UserRepositoryTest(UserRepository userRepository, JdbcClient jdbcClient) {
        this.userRepository = userRepository;
        this.jdbcClient = jdbcClient;

    }

    private long idOfTestUser1() {
        return jdbcClient.sql("SELECT id FROM users WHERE email = 'test1@example.com'")
                .query(Long.class)
                .single();
    }


    @Test
    void findByExistingIdFindsUser() {
        Optional<User> user = userRepository.findById(idOfTestUser1());
        assertThat(user)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getName()).isEqualTo("Test User 1");
                    assertThat(item.getEmail()).isEqualTo("test1@example.com");
                });
    }

    @Test
    void findByNonExistingIdFindsNoUser() {
        assertThat(userRepository.findById(Long.MAX_VALUE)).isEmpty();
    }


    @Test
    void findByExistingNameFindsUser() {
        Optional<User> user = userRepository.findByName("Test User 1");
        assertThat(user)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getEmail()).isEqualTo("test1@example.com");
                    assertThat(item.getId()).isEqualTo(idOfTestUser1());
                });
    }

    @Test
    void findByNonExistingNameFindsNoUser() {
        assertThat(userRepository.findByName("Non Existing")).isEmpty();
    }

    @Test
    void findByExistingEmailFindsUser() {
        Optional<User> user = userRepository.findByEmail("test1@example.com");
        assertThat(user)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getName()).isEqualTo("Test User 1");
                    assertThat(item.getId()).isEqualTo(idOfTestUser1());
               });
    }

    @Test
    void findByNonExistingEmailFindsNoUser() {
        assertThat(userRepository.findByEmail("non.existing@example.com")).isEmpty();
    }

    @Test
    void createMakesNewRecordInDBIfEmailIsUnique() {
        var user = new User(1, "New User", "new.user@example.com");
        long newId = userRepository.create(user);
        assertThat(newId).isPositive();
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, USERS_TABLE, "email = 'new.user@example.com'"))
                .isEqualTo(1);
    }

    @Test
    void createWithExistingEmailFails() {
        var user = new User(1, "New User", "new.user@example.com");
        long newId = userRepository.create(user);
        assertThat(newId).isPositive();
        var newUser = new User(1, "New User2", "new.user@example.com");
        assertThatExceptionOfType(DuplicateKeyException.class).isThrownBy(() -> userRepository.create(newUser));

    }


    @Test
    void findAllReturnsAllUsersSortedByName() {
        var records = JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE);
        assertThat(userRepository.findAll())
                .hasSize(records)
                .extracting(User::getName)
                .isSortedAccordingTo(String::compareToIgnoreCase);
    }


}
