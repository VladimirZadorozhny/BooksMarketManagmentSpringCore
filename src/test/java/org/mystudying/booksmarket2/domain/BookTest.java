package org.mystudying.booksmarket2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class BookTest {
    @Test
    void createBookWithCorrectDataSucceed() {
        new Book(1, "Foundation", 1951, 101, 3);
    }

    @Test
    void createBookWithoutTitleFails() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
             new Book(1, "", 1951, 101, 3);
        });
        assertThatNullPointerException().isThrownBy(() -> {
             new Book(1, null, 1951, 101, 3);
        });
    }

    @Test
    void createBookWithIncorrectYearFails() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
             new Book(1, "Foundation", 0, 101, 3);
        });
        assertThatIllegalArgumentException().isThrownBy(() -> {
            var year = LocalDate.now().getYear() + 1;
            new Book(1, "Foundation", year, 101, 3);
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -5})
    void createBookWithIncorrectBookIdFails(long bookId) {
            assertThatIllegalArgumentException().isThrownBy(() -> {
            new Book(bookId, "Foundation", 1951, 101, 3);
        });

    }

    @ParameterizedTest
    @ValueSource(longs = {0, -5})
    void createBookWithIncorrectAuthorIdFails(long authorId) {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            new Book(1, "Foundation", 1951, authorId, 3);
        });
    }

    @Test
    void createBookWithIncorrectAmountFails() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            new Book(1, "Foundation", 1951, 101, -5);
        });
    }

    @Test
    void returnBookIncreasesAvailableByOne() {
        Book book = new Book(1, "Foundation", 1951, 101, 3);
        var available = book.getAvailable();
        book.returnBook();
        assertThat(book.getAvailable()).isEqualTo(available + 1);
    }

    @Test
    void rentBookDecreasesAvailableByOne() {
        Book book = new Book(1, "Foundation", 1951, 101, 3);
        var available = book.getAvailable();
        book.rentBook();
        assertThat(book.getAvailable()).isEqualTo(available - 1);
    }

    @Test
    void rentBookWithoutStockFails() {
        Book book = new Book(1, "Foundation", 1951, 101, 0);
        assertThatIllegalArgumentException().isThrownBy(book::rentBook);
    }

}
