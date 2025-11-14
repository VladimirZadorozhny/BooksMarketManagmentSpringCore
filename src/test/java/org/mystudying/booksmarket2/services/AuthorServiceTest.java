package org.mystudying.booksmarket2.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mystudying.booksmarket2.domain.Author;
import org.mystudying.booksmarket2.domain.Book;
import org.mystudying.booksmarket2.exceptions.AuthorNotFoundException;
import org.mystudying.booksmarket2.repositories.AuthorRepository;
import org.mystudying.booksmarket2.repositories.BookRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    private AuthorService authorService;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookRepository bookRepository;

    private Author author1, author2;

    @BeforeEach
    void beforeEach() {
        authorService = new AuthorService(authorRepository, bookRepository);
        author1 = new Author(1, "Author A", LocalDate.now());
        author2 = new Author(2, "Author B", LocalDate.now());
    }

    @Test
    void findAll() {
        when(authorRepository.findAll()).thenReturn(List.of(
                author1, author2));
        assertThat(authorService.findAll()).hasSize(2);
        verify(authorRepository).findAll();
    }

    @Test
    void findByExistingIdFindsAuthor() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author1));
        assertThat(authorService.findById(1L)).contains(author1);
        verify(authorRepository).findById(1L);
    }

    @Test
    void findByNonExistingIdFindsNoOneAuthor() {
        when(authorRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        assertThat(authorService.findById(Long.MAX_VALUE)).isEmpty();
        verify(authorRepository).findById(Long.MAX_VALUE);
    }

    @Test
    void findByExistingNameFindsAuthor() {
        when(authorRepository.findByName("Author A")).thenReturn(Optional.of(author1));
        assertThat(authorService.findByName("Author A")).contains(author1);
        verify(authorRepository).findByName("Author A");
    }

    @Test
    void findByNonExistingNameFindsNoOneAuthor() {
        when(authorRepository.findByName("Non Existing")).thenReturn(Optional.empty());
        assertThat(authorService.findByName("Non Existing")).isEmpty();
        verify(authorRepository).findByName("Non Existing");
    }

    @Test
    void createMakesNewAuthor() {
        when(authorRepository.create(argThat(a ->
                a.getName().equals(author1.getName()) && a.getBirthdate().equals(author1.getBirthdate()))))
                .thenReturn(1L);

        long id = authorService.create(author1.getName(), author1.getBirthdate());
        assertThat(id).isEqualTo(1L);

        verify(authorRepository).create(argThat(a -> a.getName().equals(author1.getName()) &&
                a.getBirthdate().equals(author1.getBirthdate())));

    }

    @Test
    void findBooksByAuthorFindsAuthorsBooks() {
        Book book1 = new Book(1, "Book 1", 2000, 1, 5);
        Book book2 = new Book(2, "Book 2", 2000, 1, 5);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author1));
        when(bookRepository.findByAuthorName("Author A")).thenReturn(List.of(book1, book2));

        List<Book> books = authorService.findBooksByAuthor(1L);
        assertThat(books).containsExactly(book1, book2);
        verify(authorRepository).findById(1L);
        verify(bookRepository).findByAuthorName("Author A");
    }

    @Test
    void findBooksByAuthorThrowsExceptionIfAuthorNotFound() {
        when(authorRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        assertThatExceptionOfType(AuthorNotFoundException.class)
                .isThrownBy(() -> authorService.findBooksByAuthor(Long.MAX_VALUE));
        verify(authorRepository).findById(Long.MAX_VALUE);
    }
}
