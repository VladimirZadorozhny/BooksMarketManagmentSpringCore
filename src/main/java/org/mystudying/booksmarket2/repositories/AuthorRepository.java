package org.mystudying.booksmarket2.repositories;


import org.mystudying.booksmarket2.domain.Author;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AuthorRepository implements AuthorRepositoryInt {
    private final JdbcClient jdbcClient;

    public AuthorRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }



    @Override
    public List<Author> findAll() {
        var sql = "SELECT id, name, birthdate FROM authors ORDER BY name";
        return jdbcClient.sql(sql).query(Author.class).list();
    }

    @Override
    public Optional<Author> findById(long id) {
        var sql = "SELECT id, name, birthdate FROM authors WHERE id = ?";
        return jdbcClient.sql(sql).param(id).query(Author.class).optional();
    }

    @Override
    public Optional<Author> findByName(String name) {
        var sql = "SELECT id, name, birthdate FROM authors WHERE name = ?";
        return jdbcClient.sql(sql).param(name).query(Author.class).optional();
    }

    @Override
    public long create(Author author) {
        var sql = "INSERT INTO authors(name, birthdate) VALUES (?, ?)";
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
                .params(author.getName(), author.getBirthdate())
                .update(keyHolder);
        return keyHolder.getKey().longValue();
    }
}
