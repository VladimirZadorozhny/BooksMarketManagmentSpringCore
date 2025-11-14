-- Test Authors
INSERT INTO authors(name, birthdate) VALUES ('Test Author 1', '1901-01-01');
INSERT INTO authors(name, birthdate) VALUES ('Test Author 2', '1902-02-02');

-- Test Users
INSERT INTO users(name, email) VALUES ('Test User 1', 'test1@example.com');
INSERT INTO users(name, email) VALUES ('Test User 2', 'test2@example.com');

-- Test Books
-- Assuming the test authors get IDs 1 and 2 if the table is empty before tests.
-- This is a risk, but we will manage it in the test logic.
-- A better way is to find the author id in the test code.
INSERT INTO books(title, year, author_id, available) VALUES ('Test Book 1', 2001, (SELECT id FROM authors WHERE name='Test Author 1'), 5);
INSERT INTO books(title, year, author_id, available) VALUES ('Test Book 2', 2002, (SELECT id FROM authors WHERE name='Test Author 2'), 0);

-- Test Bookings
INSERT INTO bookings(user_id, book_id) VALUES ((SELECT id FROM users WHERE email='test1@example.com'), (SELECT id FROM books WHERE title='Test Book 1'));
