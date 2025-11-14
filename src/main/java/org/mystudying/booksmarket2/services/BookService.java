package org.mystudying.booksmarket2.services;


import org.mystudying.booksmarket2.domain.Book;
import org.mystudying.booksmarket2.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public List<Book> findByYear(int year) {
        return bookRepository.findByYear(year);
    }

    public List<Book> findByAuthorName(String authorName) {
        return bookRepository.findByAuthorName(authorName);
    }

    public List<Book> findByAvailability(boolean available) {
        return bookRepository.findByAvailability(available);
    }

    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }
}
