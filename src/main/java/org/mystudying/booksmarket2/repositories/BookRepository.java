package org.mystudying.booksmarket2.repositories;


import org.mystudying.booksmarket2.domain.Book;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository implements BookRepositoryInt {
    private final JdbcClient jdbcClient;

    public BookRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Book> findAll() {
        var sql = "SELECT id, title, year, author_id, available FROM books ORDER BY title";
        return jdbcClient.sql(sql).query(Book.class).list();
    }

    @Override
    public List<Book> findByYear(int year) {
        var sql = "SELECT id, title, year, author_id, available FROM books WHERE year = :year ORDER BY title";
        return jdbcClient.sql(sql).param("year", year).query(Book.class).list();
    }

    @Override
    public List<Book> findByAuthorName(String authorName) {
        var sql = """
                  SELECT b.id, b.title, b.year, b.author_id, b.available
                  FROM books b
                  JOIN authors a ON b.author_id = a.id
                  WHERE a.name = ?
                  ORDER BY b.title
                  """;
        return jdbcClient.sql(sql).param(authorName).query(Book.class).list();
    }

    @Override
    public List<Book> findByAvailability(boolean available) {
        var sql = "SELECT id, title, year, author_id, available FROM books WHERE available > 0 ORDER BY title";
        if (!available) {
            sql = "SELECT id, title, year, author_id, available FROM books WHERE available = 0 ORDER BY title";
        }
        return jdbcClient.sql(sql).query(Book.class).list();
    }

    @Override
    public Optional<Book> findById(long id) {
        var sql = "SELECT id, title, year, author_id, available FROM books WHERE id = :id";
        return jdbcClient.sql(sql).param("id", id).query(Book.class).optional();
    }

    @Override
    public Optional<Book> findByTitle(String title) {
        var sql = "SELECT id, title, year, author_id, available FROM books WHERE title = :title";
        return jdbcClient.sql(sql).param("title", title).query(Book.class).optional();
    }

    @Override
    public void update(Book book) {
        var sql = "UPDATE books SET title = ?, year = ?, author_id = ?, available = ? WHERE id = ?";
        jdbcClient.sql(sql)
                .params(book.getTitle(), book.getYear(),  book.getAuthorId(), book.getAvailable(), book.getId())
                .update();
    }

    @Override
    public List<Book> findBooksByUserId(long userId) {
        var sql = """
                SELECT b.id, b.title, b.year, b.author_id, b.available
                FROM books b
                JOIN bookings bk ON b.id = bk.book_id
                WHERE bk.user_id = ?
                ORDER BY b.title
                """;
        return jdbcClient.sql(sql).param(userId).query(Book.class).list();
    }


    @Override
    public Optional<Book> findAndLockById(long id) {
        var sql = "SELECT id, title, year, author_id, available FROM books WHERE id = :id for update";
        return jdbcClient.sql(sql).param("id", id).query(Book.class).optional();
    }
}
