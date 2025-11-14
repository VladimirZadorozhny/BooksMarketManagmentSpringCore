package org.mystudying.booksmarket2.services;


import org.junit.jupiter.api.Test;
import org.mystudying.booksmarket2.domain.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.mystudying.booksmarket2.exceptions.EmailAlreadyExistsException;
import org.mystudying.booksmarket2.exceptions.UserNotFoundException;
import org.mystudying.booksmarket2.exceptions.BookNotFoundException;
import org.mystudying.booksmarket2.exceptions.BookAlreadyBorrowedException;
import org.mystudying.booksmarket2.exceptions.BookNotBorrowedException;
import org.mystudying.booksmarket2.repositories.BookRepository;
import org.mystudying.booksmarket2.repositories.BookingRepository;
import org.mystudying.booksmarket2.repositories.UserRepository;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@JdbcTest
@Import({UserService.class, UserRepository.class, BookRepository.class, BookingRepository.class})
@Sql("/insertTestRecords.sql")
class UserServiceIntegrationTest {

    private static final String USERS_TABLE = "users";
    private static final String BOOKS_TABLE = "books";
    private static final String BOOKINGS_TABLE = "bookings";

    private final UserService userService;
    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    private final TransactionTemplate txTemplate;


    public UserServiceIntegrationTest(UserService userService, JdbcClient jdbcClient, JdbcTemplate jdbcTemplate, PlatformTransactionManager txManager) {
        this.userService = userService;
        this.jdbcClient = jdbcClient;
        this.jdbcTemplate = jdbcTemplate;
        this.txTemplate = new TransactionTemplate(txManager);
    }

    private long idOfTestUser1() {
        return jdbcClient.sql("SELECT id FROM users WHERE email = 'test1@example.com'")
                .query(Long.class)
                .single();
    }

    private long idOfTestUser2() {
        return jdbcClient.sql("SELECT id FROM users WHERE email = 'test2@example.com'")
                .query(Long.class)
                .single();
    }

    private long idOfTestBook1() {
        return jdbcClient.sql("SELECT id FROM books WHERE title = 'Test Book 1'")
                .query(Long.class)
                .single();
    }

    private long idOfTestBook2() {
        return jdbcClient.sql("SELECT id FROM books WHERE title = 'Test Book 2'")
                .query(Long.class)
                .single();
    }

    private void dbCleanup(long userId1, long userId2) {
        // Cleanup
        // Delete bookings first due to foreign key constraints
        JdbcTestUtils.deleteFromTableWhere(jdbcTemplate, BOOKINGS_TABLE,
                "user_id = (SELECT id FROM users WHERE email = 'test1@example.com') OR " +
                        "user_id = " + userId1 + " OR user_id = " + userId2);

        // Delete books
        JdbcTestUtils.deleteFromTableWhere(jdbcTemplate, BOOKS_TABLE,
                "title = 'Test Book 1' OR title = 'Test Book 2'");

        // Delete authors
        JdbcTestUtils.deleteFromTableWhere(jdbcTemplate, "authors",
                "name = 'Test Author 1' OR name = 'Test Author 2'");

        // Delete users
        JdbcTestUtils.deleteFromTableWhere(jdbcTemplate, USERS_TABLE,
                "email = 'test1@example.com' OR email = 'test2@example.com' OR email = 'newuser@example.com'");
    }



    @Test
    void createThrowsExceptionIfEmailExists() {
        assertThatExceptionOfType(EmailAlreadyExistsException.class)
                .isThrownBy(() -> userService.create("Test User 1", "test1@example.com"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rentBookDecreasesBookAvailableAndAddBookingAndMakesBlockForConcurrentRenting() throws Exception {

        long userId1 = idOfTestUser2();
        final long[] userId2 = {-1L}; // Declare as final array
        long bookId = idOfTestBook1();

        int initialAvailable = jdbcClient.sql("SELECT available FROM books WHERE id = ?")
                .param(bookId)
                .query(Integer.class)
                .single();

        long initialBookings = JdbcTestUtils.countRowsInTable(jdbcTemplate, BOOKINGS_TABLE);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch contenderDone = new CountDownLatch(1);

        var updateBlocked = new AtomicBoolean(false);

        try {
            userId2[0] = userService.create("New User", "newuser@example.com");

            Thread t1 = new Thread(() ->
                    txTemplate.execute(status -> {
                        userService.rentBook(userId1, bookId);
                        lockAcquired.countDown();
                        try {
                           contenderDone.await();
                        } catch (InterruptedException ignored) {}
                        return null;
                    }));

            Thread t2 = new Thread(() -> {
                try {
                    lockAcquired.await();
                    txTemplate.execute(status -> {
                        jdbcClient.sql("SET SESSION innodb_lock_wait_timeout = 2").update();
                        userService.rentBook(userId2[0], bookId);
                        return null;
                    });
                } catch (Exception e) {
                    if (e.getCause() instanceof CannotAcquireLockException ||
                            e instanceof CannotAcquireLockException) {
                        updateBlocked.set(true);
                    }
                } finally {
                    contenderDone.countDown();
                }
            });

            t1.start();
            t2.start();
            t2.join(7000);
            t1.join(3000);


            assertThat(updateBlocked.get()).isTrue();
            assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, BOOKINGS_TABLE))
                    .isEqualTo(initialBookings + 1);
            assertThat(jdbcClient.sql("SELECT available FROM books WHERE id = ?")
                    .param(bookId)
                    .query(Integer.class)
                    .single())
                    .isEqualTo(initialAvailable - 1);
            assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE,
                    "user_id = " + userId1 + " AND book_id = " + bookId)).isEqualTo(1);
            assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE,
                    "user_id = " + userId2[0] + " AND book_id = " + bookId)).isZero();

        } finally {
            dbCleanup(userId1, userId2[0]);
        }
    }





    @Test
    void rentBookThrowsExceptionIfUserNotFound() {
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userService.rentBook(Long.MAX_VALUE, idOfTestBook1()));
    }

    @Test
    void rentBookThrowsExceptionIfBookNotFound() {
        assertThatExceptionOfType(BookNotFoundException.class)
                .isThrownBy(() -> userService.rentBook(idOfTestUser1(), Long.MAX_VALUE));
    }

    @Test
    void rentBookThrowsExceptionIfAlreadyBorrowed() {
        long userId = idOfTestUser1(); // User 1 already has Book 1
        long bookId = idOfTestBook1();

        assertThatExceptionOfType(BookAlreadyBorrowedException.class)
                .isThrownBy(() -> userService.rentBook(userId, bookId));
    }

    @Test
    void rentBookThrowsExceptionIfBookNotAvailable() {
        long userId = idOfTestUser2(); // User 2 has no books initially
        long bookId = idOfTestBook2(); // Book 2 is not available (available = 0)

        assertThatExceptionOfType(BookNotFoundException.class) // Using BookNotFoundException for "not available"
                .isThrownBy(() -> userService.rentBook(userId, bookId))
                .withMessageContaining("Book is not available for rent.");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void returnBookIncreasesBookAvailableAndDeleteBookingAndMakesLockForAgainstConcurrencyChanges() throws Exception{

        long userId1 = idOfTestUser1();
        final long[] userId2 = {-1L}; // Declare as final array
        long bookId = idOfTestBook1();

        int initialAvailable = jdbcClient.sql("SELECT available FROM books WHERE id = ?")
                .param(bookId)
                .query(Integer.class)
                .single();

        long initialBookings = JdbcTestUtils.countRowsInTable(jdbcTemplate, BOOKINGS_TABLE);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch contenderDone = new CountDownLatch(1);

        var updateBlocked = new AtomicBoolean(false);

        try {
            userId2[0] = idOfTestUser2();

            Thread t1 = new Thread(() ->
                    txTemplate.execute(status -> {
                        userService.returnBook(userId1, bookId);
                        lockAcquired.countDown();
                        try {
                            contenderDone.await();
                        } catch (InterruptedException ignored) {}
                        return null;
                    }));

            Thread t2 = new Thread(() -> {
                try {
                    lockAcquired.await();
                    txTemplate.execute(status -> {
                        jdbcClient.sql("SET SESSION innodb_lock_wait_timeout = 2").update();
                        userService.rentBook(userId2[0], bookId);
                        return null;
                    });
                } catch (Exception e) {
                    if (e.getCause() instanceof CannotAcquireLockException ||
                            e instanceof CannotAcquireLockException) {
                        updateBlocked.set(true);
                    }
                } finally {
                    contenderDone.countDown();
                }
            });

            t1.start();
            t2.start();
            t2.join(7000);
            t1.join(3000);


            assertThat(updateBlocked.get()).isTrue();
            assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, BOOKINGS_TABLE))
                    .isEqualTo(initialBookings - 1);
            assertThat(jdbcClient.sql("SELECT available FROM books WHERE id = ?")
                    .param(bookId)
                    .query(Integer.class)
                    .single())
                    .isEqualTo(initialAvailable + 1);
            assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE,
                    "user_id = " + userId1 + " AND book_id = " + bookId)).isZero();
            assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE,
                    "user_id = " + userId2[0] + " AND book_id = " + bookId)).isZero();

        } finally {
            dbCleanup(userId1, userId2[0]);
        }

    }

    @Test
    void returnBookThrowsExceptionIfUserNotFound() {
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userService.returnBook(Long.MAX_VALUE, idOfTestBook1()));
    }

    @Test
    void returnBookThrowsExceptionIfBookNotFound() {
        assertThatExceptionOfType(BookNotFoundException.class)
                .isThrownBy(() -> userService.returnBook(idOfTestUser1(), Long.MAX_VALUE));
    }

    @Test
    void returnBookThrowsExceptionIfBookNotBorrowed() {
        long userId = idOfTestUser2(); // User 2 does not have Book 1
        long bookId = idOfTestBook1();

        assertThatExceptionOfType(BookNotBorrowedException.class)
                .isThrownBy(() -> userService.returnBook(userId, bookId));
    }


    @Test
    void createWithExistingEmailFails() {
        long newId = userService.create("New User", "new.user@example.com");
        assertThat(newId).isPositive();
        assertThatExceptionOfType(EmailAlreadyExistsException.class).isThrownBy(() -> userService.create("New User", "new.user@example.com"));
    }
}
