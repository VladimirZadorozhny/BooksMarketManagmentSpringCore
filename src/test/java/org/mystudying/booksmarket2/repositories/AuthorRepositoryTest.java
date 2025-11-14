package org.mystudying.booksmarket2.repositories;


import org.junit.jupiter.api.Test;
import org.mystudying.booksmarket2.domain.Author;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(AuthorRepository.class)
@Sql("/insertTestRecords.sql")
class AuthorRepositoryTest {
    private static final String AUTHORS_TABLE = "authors";
    private final AuthorRepositoryInt authorRepository;
    private final JdbcClient jdbcClient;

    public AuthorRepositoryTest(AuthorRepositoryInt authorRepository, JdbcClient jdbcClient) {
        this.authorRepository = authorRepository;
        this.jdbcClient = jdbcClient;
    }

    private long idOfTestAuthor1() {
        return jdbcClient.sql("SELECT id FROM authors WHERE name = 'Test Author 1'")
                .query(Long.class)
                .single();
    }

    @Test
    void findByExistingIdFindsAuthor() {
        Optional<Author> author = authorRepository.findById(idOfTestAuthor1());
        assertThat(author)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getName()).isEqualTo("Test Author 1");
                    assertThat(item.getId()).isEqualTo(idOfTestAuthor1());
                });
    }

    @Test
    void findByNonExistingIdFindsNoOne() {
        assertThat(authorRepository.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void findByExistingNameFindsAuthor() {
        Optional<Author> author = authorRepository.findByName("Test Author 1");
        assertThat(author)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getBirthdate()).isEqualTo(LocalDate.of(1901, 1, 1));
                    assertThat(item.getId()).isEqualTo(idOfTestAuthor1());
               });
    }

    @Test
    void findByNonExistingNameFindsNoOne() {
        assertThat(authorRepository.findByName("Non Existing")).isEmpty();
    }

    @Test
    void createMakeNewAuthorAndAddRecordToDatabase() {
        var author = new Author(1, "New Author", LocalDate.now());
        long newId = authorRepository.create(author);
        assertThat(newId).isPositive();
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, AUTHORS_TABLE, "name = 'New Author' and id = " + newId))
                .isEqualTo(1);
    }

    @Test
    void findAllReturnsAllAuthorsSortedByNames() {
        var amountRecords = JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORS_TABLE);
        assertThat(authorRepository.findAll())
                .hasSize(amountRecords)
                .extracting(Author::getName)
                .isSorted();

    }
}
