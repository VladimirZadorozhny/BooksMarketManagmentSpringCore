package org.mystudying.booksmarket2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BookingTest {
    @Test
    void createBookingWithCorrectDataSucceed() {
        new Booking(1, 101);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -10})
    void createBookingWithIncorrectUserIdFail(long userId) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Booking(userId, 101));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -10})
    void createBookingWithIncorrectBookIdFail(long bookId) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Booking(1, bookId));
    }
}
