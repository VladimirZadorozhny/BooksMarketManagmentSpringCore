package org.mystudying.booksmarket2.repositories;


import org.junit.jupiter.api.Test;
import org.mystudying.booksmarket2.domain.Booking;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(BookingRepository.class)
@Sql("/insertTestRecords.sql")
class BookingRepositoryTest {
    private static final String BOOKINGS_TABLE = "bookings";
    private final BookingRepository bookingRepository;
    private final JdbcClient jdbcClient;

    public BookingRepositoryTest(BookingRepository bookingRepository, JdbcClient jdbcClient) {
        this.bookingRepository = bookingRepository;
        this.jdbcClient = jdbcClient;
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

    @Test
    void createAddsNewBooking() {
        long userId = idOfTestUser2();
        long bookId = idOfTestBook1();
        var booking = new Booking(userId, bookId);
        bookingRepository.create(booking);
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE,
                "user_id = " + userId + " AND book_id = " + bookId)).isOne();
    }

    @Test
    void deleteRemovesOneBooking() {
        long userId = idOfTestUser1();
        long bookId = idOfTestBook1();
        var booking = new Booking(userId, bookId);
        bookingRepository.delete(booking);
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE,
                "user_id = " + userId + " AND book_id = " + bookId)).isZero();
    }

    @Test
    void findFindsTheBooking() {
        long userId = idOfTestUser1();
        long bookId = idOfTestBook1();
        assertThat(bookingRepository.find(userId, bookId)).isPresent();
    }

    @Test
    void findNonExistingFindsNoBooking() {
        assertThat(bookingRepository.find(Long.MAX_VALUE, Long.MAX_VALUE)).isEmpty();
    }
}
