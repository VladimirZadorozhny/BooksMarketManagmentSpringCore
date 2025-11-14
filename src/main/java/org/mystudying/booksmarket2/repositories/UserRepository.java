package org.mystudying.booksmarket2.repositories;


import org.mystudying.booksmarket2.domain.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository implements UserRepositoryInt {
    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<User> findAll() {
        var sql = "SELECT id, name, email FROM users ORDER BY name";
        return jdbcClient.sql(sql).query(User.class).list();
    }

    @Override
    public Optional<User> findById(long id) {
        var sql = "SELECT id, name, email FROM users WHERE id = ?";
        return jdbcClient.sql(sql).param(id).query(User.class).optional();
    }

    @Override
    public Optional<User> findByName(String name) {
        var sql = "SELECT id, name, email FROM users WHERE name = ?";
        return jdbcClient.sql(sql).param(name).query(User.class).optional();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        var sql = "SELECT id, name, email FROM users WHERE email = ?";
        return jdbcClient.sql(sql).param(email).query(User.class).optional();
    }

    @Override
    public long create(User user) {
        var sql = "INSERT INTO users(name, email) VALUES (?, ?)";
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
                .params(user.getName(), user.getEmail())
                .update(keyHolder);
        return keyHolder.getKey().longValue();
    }
}
