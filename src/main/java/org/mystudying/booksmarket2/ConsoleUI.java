package org.mystudying.booksmarket2;


import org.mystudying.booksmarket2.domain.Author;
import org.mystudying.booksmarket2.domain.Book;
import org.mystudying.booksmarket2.domain.User;
import org.mystudying.booksmarket2.exceptions.*;
import org.mystudying.booksmarket2.services.AuthorService;
import org.mystudying.booksmarket2.services.BookService;
import org.mystudying.booksmarket2.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class ConsoleUI implements CommandLineRunner {

    private final UserService userService;
    private final AuthorService authorService;
    private final BookService bookService;
    private final Scanner scanner;

    public ConsoleUI(UserService userService, AuthorService authorService, BookService bookService) {
        this.userService = userService;
        this.authorService = authorService;
        this.bookService = bookService;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run(String... args) {
        displayMainMenu();
    }

    private void displayMainMenu() {
        int choice;
        do {
            System.out.println("\n--- Books Market Main Menu ---");
            System.out.println("1. User Management");
            System.out.println("2. Author Management");
            System.out.println("3. Book Management");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            choice = getIntInput();

            switch (choice) {
                case 1 -> userMenu();
                case 2 -> authorMenu();
                case 3 -> bookMenu();
                case 0 -> System.out.println("Exiting application. Goodbye!");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);
    }

    private void userMenu() {
        int choice;
        do {
            System.out.println("\n--- User Management ---");
            System.out.println("1. Show all users");
            System.out.println("2. Find user by ID");
            System.out.println("3. Find user by Name/Email");
            System.out.println("4. Show books by a user");
            System.out.println("5. Rent a book");
            System.out.println("6. Return a book");
            System.out.println("7. Add new user");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");
            choice = getIntInput();

            switch (choice) {
                case 1 -> showAllUsers();
                case 2 -> findUserById();
                case 3 -> findUserByNameOrEmail();
                case 4 -> showBooksByUser();
                case 5 -> rentBook();
                case 6 -> returnBook();
                case 7 -> addNewUser();
                case 0 -> System.out.println("Returning to Main Menu.");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);
    }

    private void authorMenu() {
        int choice;
        do {
            System.out.println("\n--- Author Management ---");
            System.out.println("1. Show all authors");
            System.out.println("2. Find author by ID");
            System.out.println("3. Find author by Name");
            System.out.println("4. Show all books by an author");
            System.out.println("5. Add new author");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");
            choice = getIntInput();

            switch (choice) {
                case 1 -> showAllAuthors();
                case 2 -> findAuthorById();
                case 3 -> findAuthorByName();
                case 4 -> showBooksByAuthor();
                case 5 -> addNewAuthor();
                case 0 -> System.out.println("Returning to Main Menu.");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);
    }

    private void bookMenu() {
        int choice;
        do {
            System.out.println("\n--- Book Management ---");
            System.out.println("1. Show all books");
            System.out.println("2. Find book by ID");
            System.out.println("3. Find book by Title");
            System.out.println("4. Show books by year");
            System.out.println("5. Show books by author name");
            System.out.println("6. Show available books");
            System.out.println("7. Show unavailable books");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");
            choice = getIntInput();

            switch (choice) {
                case 1 -> showAllBooks();
                case 2 -> findBookById();
                case 3 -> findBookByTitle();
                case 4 -> showBooksByYear();
                case 5 -> showBooksByAuthorName();
                case 6 -> showAvailableBooks();
                case 7 -> showUnavailableBooks();
                case 0 -> System.out.println("Returning to Main Menu.");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);
    }

    // --- User Operations ---
    private void showAllUsers() {
        List<User> users = userService.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            users.forEach(System.out::println);
        }
    }

    private void findUserById() {
        System.out.print("Enter user ID: ");
        long id = getLongInput();
        try {
            var user = userService.findById(id).orElseThrow(() -> new UserNotFoundException(id));
            System.out.println(user);
        } catch (UserNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }


    private void findUserByNameOrEmail() {
        System.out.print("Enter user name or email: ");
        String input = scanner.nextLine();
        try {
            Optional<User> user = userService.findByName(input);
            if (user.isPresent()) {
                System.out.println(user.get());
            } else {
                System.out.println(userService.findByEmail(input).orElseThrow(() -> new UserNotFoundException(input)));
            }
        } catch (UserNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void showBooksByUser() {
        System.out.print("Enter user ID: ");
        long userId = getLongInput();
        try {
            List<Book> books = userService.findBooksByUserId(userId);
            if (books.isEmpty()) {
                System.out.println("User " + userId + " has not borrowed any books.");
            } else {
                System.out.println("Books borrowed by user " + userId + ":");
                books.forEach(System.out::println);
            }
        } catch (UserNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void rentBook() {
        System.out.print("Enter user ID: ");
        long userId = getLongInput();
        System.out.print("Enter book ID: ");
        long bookId = getLongInput();
        try {
            userService.rentBook(userId, bookId);
            System.out.println("Book " + bookId + " rented by user " + userId + " successfully.");
        } catch (UserNotFoundException | BookNotFoundException | BookAlreadyBorrowedException e) {
            System.out.println("Error renting book: " + e.getMessage());
        }
    }

    private void returnBook() {
        System.out.print("Enter user ID: ");
        long userId = getLongInput();
        System.out.print("Enter book ID: ");
        long bookId = getLongInput();
        try {
            userService.returnBook(userId, bookId);
            System.out.println("Book " + bookId + " returned by user " + userId + " successfully.");
        } catch (UserNotFoundException | BookNotFoundException | BookNotBorrowedException e) {
            System.out.println("Error returning book: " + e.getMessage());
        }
    }


    private void addNewUser() {
        System.out.print("Enter user name: ");
        String name = scanner.nextLine();
        System.out.print("Enter user email: ");
        String email = scanner.nextLine();
        try {
            long newId = userService.create(name, email);
            System.out.println("New user added with ID: " + newId);
        } catch (EmailAlreadyExistsException | IllegalArgumentException e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }

    // --- Author Operations ---
    private void showAllAuthors() {
        List<Author> authors = authorService.findAll();
        if (authors.isEmpty()) {
            System.out.println("No authors found.");
        } else {
            authors.forEach(System.out::println);
        }
    }

    private void findAuthorById() {
        System.out.print("Enter author ID: ");
        long id = getLongInput();
        try {
            var author = authorService.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
            System.out.println(author);
        } catch (AuthorNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    private void findAuthorByName() {
        System.out.print("Enter author name: ");
        String name = scanner.nextLine();
        try {
            var author = authorService.findByName(name).orElseThrow(() -> new AuthorNotFoundException(name));
            System.out.println(author);
        }  catch (AuthorNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    private void showBooksByAuthor() {
        System.out.print("Enter author ID: ");
        long authorId = getLongInput();
        try {
            List<Book> books = authorService.findBooksByAuthor(authorId);
            if (books.isEmpty()) {
                System.out.println("No books found for author ID: " + authorId);
            } else {
                System.out.println("Books by author ID " + authorId + ":");
                books.forEach(System.out::println);
            }
        } catch (AuthorNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }


    private void addNewAuthor() {
        System.out.print("Enter author name: ");
        String name = scanner.nextLine();
        System.out.print("Enter author birthdate (YYYY-MM-DD): ");
        String birthdateString = scanner.nextLine();
        try {
            LocalDate birthdate = LocalDate.parse(birthdateString);
            long newId = authorService.create(name, birthdate);
            System.out.println("New author added with ID: " + newId);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- Book Operations ---
    private void showAllBooks() {
        List<Book> books = bookService.findAll();
        if (books.isEmpty()) {
            System.out.println("No books found.");
        } else {
            books.forEach(System.out::println);
        }
    }

    private void findBookById() {
        System.out.print("Enter book ID: ");
        long id = getLongInput();
        try {
            var book =  bookService.findById(id).orElseThrow(() -> new BookNotFoundException(id));
            System.out.println(book);
        } catch (BookNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void findBookByTitle() {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        try {
            var  book = bookService.findByTitle(title).orElseThrow(() -> new BookNotFoundException(title));
            System.out.println(book);
        } catch (BookNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void showBooksByYear() {
        System.out.print("Enter year: ");
        int year = getIntInput();
        List<Book> books = bookService.findByYear(year);
        if (books.isEmpty()) {
            System.out.println("No books found for year: " + year);
        } else {
            books.forEach(System.out::println);
        }
    }

    private void showBooksByAuthorName() {
        System.out.print("Enter author name: ");
        String authorName = scanner.nextLine();
        List<Book> books = bookService.findByAuthorName(authorName);
        if (books.isEmpty()) {
            System.out.println("No books found for author: " + authorName);
        } else {
            books.forEach(System.out::println);
        }
    }

    private void showAvailableBooks() {
        List<Book> books = bookService.findByAvailability(true);
        if (books.isEmpty()) {
            System.out.println("No available books found.");
        } else {
            books.forEach(System.out::println);
        }
    }

    private void showUnavailableBooks() {
        List<Book> books = bookService.findByAvailability(false);
        if (books.isEmpty()) {
            System.out.println("No unavailable books found.");
        } else {
            books.forEach(System.out::println);
        }
    }

    // --- Helper methods for input ---
    private int getIntInput() {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline left-over
                return input;
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine(); // Consume the invalid input
            }
        }
    }

    private long getLongInput() {
        while (true) {
            try {
                long input = scanner.nextLong();
                scanner.nextLine(); // Consume newline left-over
                return input;
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine(); // Consume the invalid input
            }
        }
    }
}
