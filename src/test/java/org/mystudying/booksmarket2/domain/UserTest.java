package org.mystudying.booksmarket2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class UserTest {
    @Test
    void createUserWithCorrectDataSucceed() {
        new User(1, "Alice Johnson", "alice@example.com");
    }



    @Test
    void createUserWithoutNameFails() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            new User(1, "", "alice@example.com");
        });
        assertThatNullPointerException().isThrownBy(() -> {
            new User(1, null, "alice@example.com");
        });
    }

    @Test
    void createUserWithIncorrectEmailFails() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            new User(1, "Alice Johnson", "@alice@example@.com");
        });
        assertThatIllegalArgumentException().isThrownBy(() -> {
            new User(1, "Alice Johnson", null);
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -5})
    void createUserWithIncorrectIdFails(long userId) {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            new User(userId, "Alice Johnson", "alice@example.com");
        });

    }



}
