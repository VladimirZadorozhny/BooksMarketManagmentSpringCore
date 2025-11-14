package org.mystudying.booksmarket2.services;


import org.junit.jupiter.api.Test;
import org.mystudying.booksmarket2.domain.Author;
import org.mystudying.booksmarket2.domain.Book;
import org.mystudying.booksmarket2.exceptions.AuthorNotFoundException;
import org.mystudying.booksmarket2.repositories.AuthorRepository;
import org.mystudying.booksmarket2.repositories.BookRepository;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@JdbcTest
@Import({AuthorService.class, AuthorRepository.class, BookRepository.class})
@Sql("/insertTestRecords.sql")
class AuthorServiceIntegrationTest {

    private final AuthorService authorService;
    private final JdbcClient jdbcClient;

    public AuthorServiceIntegrationTest(AuthorService authorService, JdbcClient jdbcClient) {
        this.authorService = authorService;
        this.jdbcClient = jdbcClient;
    }

    private long idOfTestAuthor1() {
        return jdbcClient.sql("SELECT id FROM authors WHERE name = 'Test Author 1'")
                .query(Long.class)
                .single();
    }

    @Test
    void findByExistingIdFindsAuthor() {
        Optional<Author> author = authorService.findById(idOfTestAuthor1());
        assertThat(author)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getName()).isEqualTo("Test Author 1");
                    assertThat(item.getId()).isEqualTo(idOfTestAuthor1());
                });
    }

    @Test
    void findByNonExistingIdFindsNoOne() {
        assertThat(authorService.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void findByExistingNameFindsAuthor() {
        Optional<Author> author = authorService.findByName("Test Author 1");
        assertThat(author)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getBirthdate()).isEqualTo(LocalDate.of(1901, 1, 1));
                    assertThat(item.getId()).isEqualTo(idOfTestAuthor1());
                });
    }

    @Test
    void findByNonExistingNameFindsNoOne() {
        assertThat(authorService.findByName("Non Existing")).isEmpty();
    }

    @Test
    void createMakeNewAuthorAndAddRecordToDatabase() {
        long newId = authorService.create("New Author", LocalDate.now());
        assertThat(newId).isPositive();
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, "authors", "name = 'New Author' and id = " + newId))
                .isEqualTo(1);
    }

    @Test
    void findAllReturnsAllAuthorsSortedByNames() {
        var amountRecords = JdbcTestUtils.countRowsInTable(jdbcClient, "authors");
        assertThat(authorService.findAll())
                .hasSize(amountRecords)
                .extracting(Author::getName)
                .isSorted();
    }

    @Test
    void findBooksByAuthor() {
        List<Book> books = authorService.findBooksByAuthor(idOfTestAuthor1());
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Test Book 1");
    }

    @Test
    void findBooksByAuthorThrowsExceptionIfAuthorNotFound() {
        assertThatExceptionOfType(AuthorNotFoundException.class)
                .isThrownBy(() -> authorService.findBooksByAuthor(Long.MAX_VALUE));
    }
}
