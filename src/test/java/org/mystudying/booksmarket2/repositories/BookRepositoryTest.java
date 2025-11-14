package org.mystudying.booksmarket2.repositories;

import org.junit.jupiter.api.Test;
import org.mystudying.booksmarket2.domain.Book;
import org.mystudying.booksmarket2.exceptions.BookNotFoundException;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@Import(BookRepository.class)
@Sql("/insertTestRecords.sql")
class BookRepositoryTest {
    private static final String BOOKS_TABLE = "books";
    private final BookRepository bookRepository;
    private final JdbcClient jdbcClient;

    public BookRepositoryTest(BookRepository bookRepository, JdbcClient jdbcClient) {
        this.bookRepository = bookRepository;
        this.jdbcClient = jdbcClient;
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

    private long idOfTestAuthor1() {
        return jdbcClient.sql("SELECT id FROM authors WHERE name = 'Test Author 1'")
                .query(Long.class)
                .single();
    }

    @Test
    void findByExistingIdFindsBook() {
        Optional<Book> book = bookRepository.findById(idOfTestBook1());
        assertThat(book)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getTitle()).isEqualTo("Test Book 1");
                    assertThat(item.getId()).isEqualTo(idOfTestBook1());
                    assertThat(item.getAuthorId()).isEqualTo(idOfTestAuthor1());
                });
    }



    @Test
    void findByNonExistingIdFindsNoBook() {
        assertThat(bookRepository.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void findByExistingTitleFindsBook() {
        Optional<Book> book = bookRepository.findByTitle("Test Book 1");
        assertThat(book)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getTitle()).isEqualTo("Test Book 1");
                    assertThat(item.getYear()).isEqualTo(2001);
                });
    }

    @Test
    void findByNonExistingTitleFindsNoBook() {
        assertThat(bookRepository.findByTitle("Non Existing")).isEmpty();
    }

    @Test
    void findByYearFindsBook() {
        assertThat(bookRepository.findByYear(2001)).hasSize(1);
        assertThat(bookRepository.findByYear(2001).get(0).getTitle()).isEqualTo("Test Book 1");
    }

    @Test
    void findByAuthorNameFindsBook() {
        assertThat(bookRepository.findByAuthorName("Test Author 1")).hasSize(1);
        assertThat(bookRepository.findByAuthorName("Test Author 1").get(0).getTitle()).isEqualTo("Test Book 1");
    }

    @Test
    void findByAvailabilityTrue() {
        var recordsAvailable = JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKS_TABLE, "available > 0");
        var recordsNoStock = JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKS_TABLE, "available = 0");
        assertThat(bookRepository.findByAvailability(true))
                .hasSize(recordsAvailable)
                .extracting(Book::getTitle)
                .isSortedAccordingTo(String::compareToIgnoreCase); // Test Book 1 is available
        assertThat(bookRepository.findByAvailability(true))
                .anySatisfy(item -> {
                    assertThat(item.getTitle()).isEqualTo("Test Book 1");
                    assertThat(item.getYear()).isEqualTo(2001);
                    assertThat(item.getId()).isEqualTo(idOfTestBook1());
                });
    }

    @Test
    void findByAvailabilityFalse() {
        var recordsNoStock = JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKS_TABLE, "available = 0");
        assertThat(bookRepository.findByAvailability(false))
                .hasSize(recordsNoStock)
                .extracting(Book::getTitle)
                .isSortedAccordingTo(String::compareToIgnoreCase); // Test Book 2 is not available
        assertThat(bookRepository.findByAvailability(false))
                .anySatisfy(item -> {
                    assertThat(item.getTitle()).isEqualTo("Test Book 2");
                    assertThat(item.getYear()).isEqualTo(2002);
                    assertThat(item.getId()).isEqualTo(idOfTestBook2());
                });
    }



    @Test
    void updateChangesBookInfo() {
        var bookId1 = idOfTestBook1();
        var book = bookRepository.findById(bookId1).orElseThrow(() -> new BookNotFoundException(bookId1));
        var newBook = new Book(book.getId(), book.getTitle() + " changed", book.getYear(), book.getAuthorId(), book.getAvailable());
        bookRepository.update(newBook);
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKS_TABLE, "title = 'Test Book 1 changed' and id = " + bookId1)).isOne();

    }

    @Test
    void findBooksByUserId() {
        long userId = jdbcClient.sql("SELECT id FROM users WHERE email = 'test1@example.com'")
                .query(Long.class)
                .single();
        var records = JdbcTestUtils.countRowsInTableWhere(jdbcClient, "bookings", "user_id = " + userId);

        assertThat(bookRepository.findBooksByUserId(userId)).hasSize(records)
                .extracting(Book::getTitle)
                .isSortedAccordingTo(String::compareToIgnoreCase);
    }

    @Test
    void findAllReturnsAllBooksSortedByTitle() {
        var records = JdbcTestUtils.countRowsInTable(jdbcClient, BOOKS_TABLE);
        assertThat(bookRepository.findAll())
                .hasSize(records)
                .extracting(Book::getTitle)
                .isSortedAccordingTo(String::compareToIgnoreCase);
    }


    @Test
    void findAndLockByIdFindsBookAndMakesItImmutable() {
        var bookId = idOfTestBook1();
        var initialAvailable = jdbcClient.sql("SELECT available FROM books WHERE id = ?")
                .param(bookId)
                .query(Integer.class)
                .single();

        Optional<Book> book = bookRepository.findAndLockById(bookId);
        assertThat(book)
                .isNotEmpty()
                .hasValueSatisfying(item -> {
                    assertThat(item.getTitle()).isEqualTo("Test Book 1");
                    assertThat(item.getId()).isEqualTo(bookId);
                    assertThat(item.getAuthorId()).isEqualTo(idOfTestAuthor1());
                    assertThat(item.getAvailable()).isEqualTo(initialAvailable);
                });

        var updateBlocked = new AtomicBoolean(false);

        Thread concurrentThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                jdbcClient.sql("set innodb_lock_wait_timeout = 2").update();
                jdbcClient.sql("update books set available = available - 1 where id = ?")
                        .param(bookId)
                        .update();

            } catch (Exception e) {
                if (e instanceof CannotAcquireLockException) {
                    updateBlocked.set(true);
                }
            }
        });

        concurrentThread.start();
        try {
            concurrentThread.join(3000);
            assertThat(updateBlocked.get()).isTrue();
            var bookAgain = jdbcClient.sql("select id, title, year, author_id, available from books where id = ?")
                    .param(bookId)
                    .query(Book.class)
                    .optional()
                    .orElseThrow(() -> new BookNotFoundException(bookId));
            assertThat(bookAgain.getAvailable()).isEqualTo(initialAvailable);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}
