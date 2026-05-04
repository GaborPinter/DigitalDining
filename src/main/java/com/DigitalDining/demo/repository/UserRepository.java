package com.DigitalDining.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DigitalDining.demo.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndEmail(String username, String email);
    
    List<User> findAllByWeeklyMenuSubscribedTrue();
}
