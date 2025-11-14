package org.mystudying.booksmarket2.exceptions;

public class BookAlreadyBorrowedException extends RuntimeException {
    public BookAlreadyBorrowedException() {
        super("Book is already borrowed by this user.");
    }
}
