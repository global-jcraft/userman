package com.huddey.core.userman.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.huddey.core.userman.data.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  @Query(
      "SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.credentials c LEFT JOIN FETCH c.authProvider WHERE u.email = :email")
  Optional<User> findByEmailWithRolesAndCredentials(@Param("email") String email);

  boolean existsByEmail(String email);

  Optional<User> findByEmailVerificationToken(String token);
}
