package org.mystudying.booksmarket2.repositories;


import org.mystudying.booksmarket2.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryInt {
    List<User> findAll();
    Optional<User> findById(long id);
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    long create(User user);
}
