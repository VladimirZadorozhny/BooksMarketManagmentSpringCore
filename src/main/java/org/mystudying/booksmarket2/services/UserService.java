package org.mystudying.booksmarket2.services;


import org.mystudying.booksmarket2.domain.Book;
import org.mystudying.booksmarket2.domain.Booking;
import org.mystudying.booksmarket2.domain.User;
import org.mystudying.booksmarket2.exceptions.*;
import org.mystudying.booksmarket2.repositories.BookRepository;
import org.mystudying.booksmarket2.repositories.BookingRepository;
import org.mystudying.booksmarket2.repositories.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookingRepository bookingRepository;

    public UserService(UserRepository userRepository, BookRepository bookRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<Book> findBooksByUserId(long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return bookRepository.findBooksByUserId(userId);
    }

    @Transactional
    public long create(String name, String email) {
        try {
            User user = new User(1, name, email);
            return userRepository.create(user);
        } catch (DuplicateKeyException e) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    @Transactional
    public void rentBook(long userId, long bookId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Book book = bookRepository.findAndLockById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        if (book.getAvailable() <= 0) {
            throw new BookNotFoundException("Book is not available for rent.");
        }

        if (bookingRepository.find(user.getId(), book.getId()).isPresent()) {
            throw new BookAlreadyBorrowedException();
        }

        bookingRepository.create(new Booking(user.getId(), book.getId()));
        book.rentBook();
        bookRepository.update(book);
    }

    @Transactional
    public void returnBook(long userId, long bookId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Book book = bookRepository.findAndLockById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        if (bookingRepository.find(user.getId(), book.getId()).isEmpty()) {
            throw new BookNotBorrowedException();
        }

        bookingRepository.delete(new Booking(user.getId(), book.getId()));
        book.returnBook();
        bookRepository.update(book);
    }
}
