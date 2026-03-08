package com.e611.toponavi.web.repository;

import com.e611.toponavi.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByGithubId(Long githubId);
    Optional<User> findByGithubLogin(String githubLogin);
}

