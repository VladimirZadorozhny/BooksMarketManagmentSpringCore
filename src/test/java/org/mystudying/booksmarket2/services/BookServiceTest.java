package org.mystudying.booksmarket2.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mystudying.booksmarket2.domain.Book;
import org.mystudying.booksmarket2.repositories.BookRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private BookService bookService;
    @Mock
    private BookRepository bookRepository;

    @BeforeEach
    void beforeEach() {
        bookService = new BookService(bookRepository);
    }

    @Test
    void findAll() {
        when(bookRepository.findAll()).thenReturn(List.of(
                new Book(1, "Book A", 2000, 1, 5),
                new Book(2, "Book B", 2001, 2, 3)
        ));
        assertThat(bookService.findAll()).hasSize(2);
        verify(bookRepository).findAll();
    }

    @Test
    void findById() {
        Book book = new Book(1, "Book A", 2000, 1, 5);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        assertThat(bookService.findById(1L)).contains(book);
        verify(bookRepository).findById(1L);
    }

    @Test
    void findByNonExistingId() {
        when(bookRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        assertThat(bookService.findById(Long.MAX_VALUE)).isEmpty();
        verify(bookRepository).findById(Long.MAX_VALUE);
    }
}
