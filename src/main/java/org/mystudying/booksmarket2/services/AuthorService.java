package org.mystudying.booksmarket2.services;


import org.mystudying.booksmarket2.domain.Author;
import org.mystudying.booksmarket2.domain.Book;
import org.mystudying.booksmarket2.exceptions.AuthorNotFoundException;
import org.mystudying.booksmarket2.repositories.AuthorRepository;
import org.mystudying.booksmarket2.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    public Optional<Author> findById(long id) {
        return authorRepository.findById(id);
    }

    public Optional<Author> findByName(String name) {
        return authorRepository.findByName(name);
    }

    public List<Book> findBooksByAuthor(long authorId) {
        var author = authorRepository.findById(authorId).orElseThrow(() -> new AuthorNotFoundException(authorId));
        return bookRepository.findByAuthorName(author.getName());
    }

    @Transactional
    public long create(String name, LocalDate birthdate) {
        Author author = new Author(1, name, birthdate);
        return authorRepository.create(author);
    }
}
