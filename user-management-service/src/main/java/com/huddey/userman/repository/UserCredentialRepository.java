package com.huddey.userman.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huddey.userman.data.entity.UserCredential;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {}
