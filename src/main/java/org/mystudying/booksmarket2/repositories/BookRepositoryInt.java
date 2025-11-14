package org.mystudying.booksmarket2.repositories;


import org.mystudying.booksmarket2.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepositoryInt {
    List<Book> findAll();
    List<Book> findByYear(int year);
    List<Book> findByAuthorName(String authorName);
    List<Book> findByAvailability(boolean available);
    Optional<Book> findById(long id);
    Optional<Book> findByTitle(String title);
    void update(Book book);
    List<Book> findBooksByUserId(long userId);

    Optional<Book> findAndLockById(long id);

}
