package com.huddey.core.userman.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huddey.core.userman.data.entity.AuthProvider;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {
  Optional<AuthProvider> findByName(String name);
}
