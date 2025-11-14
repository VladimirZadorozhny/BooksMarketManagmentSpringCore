package org.mystudying.booksmarket2.repositories;


import org.mystudying.booksmarket2.domain.Booking;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class BookingRepository implements BookingRepositoryInt {
    private final JdbcClient jdbcClient;

    public BookingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }



    @Override
    public void create(Booking booking) {
        var sql = "INSERT INTO bookings(user_id, book_id) VALUES (:userId, :bookId)";
        jdbcClient.sql(sql)
                .params(Map.of("userId", booking.getUserId(),
                "bookId", booking.getBookId()))
                .update();
    }

    @Override
    public void delete(Booking booking) {
        var sql = "DELETE FROM bookings WHERE user_id = ? AND book_id = ?";
        jdbcClient.sql(sql)
                .params(booking.getUserId(), booking.getBookId())
                .update();
    }

    @Override
    public Optional<Booking> find(long userId, long bookId) {
        var sql = "SELECT user_id, book_id FROM bookings WHERE user_id = ? AND book_id = ?";
        return jdbcClient.sql(sql)
                .params(userId, bookId)
                .query(Booking.class)
                .optional();
    }
}
