package org.mystudying.booksmarket2.repositories;

import org.mystudying.booksmarket2.domain.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorRepositoryInt {
    List<Author> findAll();
    Optional<Author> findById(long id);
    Optional<Author> findByName(String name);
    long create(Author author);
}
