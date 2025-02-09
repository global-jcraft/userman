package com.huddey.core.userman.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huddey.core.userman.data.entity.UserCredential;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {}
