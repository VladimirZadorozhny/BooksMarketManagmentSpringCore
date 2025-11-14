package org.mystudying.booksmarket2.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class AuthorTest {
    @Test
    void createAuthorWithCorrectDataSucceed() {
        LocalDate birthdate = LocalDate.of(1920, 1, 2);
        Author author = new Author(1, "Isaac Asimov", birthdate);

    }

    @Test
    void createAuthorWithoutNameFail() {
        LocalDate birthdate = LocalDate.of(1920, 1, 2);
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Author author = new Author(1, "", birthdate);
        });
        assertThatNullPointerException().isThrownBy(() -> {
            Author author = new Author(1, null, birthdate);
        });
    }

    @Test
    void createAuthorWithoutBirthdateFail() {
        assertThatNullPointerException().isThrownBy(() -> {
            Author author = new Author(1, "Isaac Asimov", null);
        });
    }

    @Test
    void createAuthorWithBirthdateFromFutureFail() {
        LocalDate birthdate = LocalDate.now().plusDays(1);
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Author author = new Author(1, "Isaac Asimov", birthdate);
        });
    }

    @Test
    void createAuthorWithIncorrectIdFail() {
        LocalDate birthdate = LocalDate.of(1920, 1, 2);
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Author author = new Author(-5, "Isaac Asimov", birthdate);
        });

    }



}
