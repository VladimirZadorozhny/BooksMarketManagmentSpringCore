package org.mystudying.booksmarket2.repositories;

import org.mystudying.booksmarket2.domain.Booking;

import java.util.Optional;

public interface BookingRepositoryInt {
    void create(Booking booking);
    void delete(Booking booking);
    Optional<Booking> find(long userId, long bookId);
}
