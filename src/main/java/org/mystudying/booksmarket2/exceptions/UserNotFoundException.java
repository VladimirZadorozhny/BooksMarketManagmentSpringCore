package org.mystudying.booksmarket2.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long id) {
        super("User not found. Id: " + id);
    }
    public UserNotFoundException(String name) {
        super("User not found. Name or email: " + name);
    }
}
