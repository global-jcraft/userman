package com.huddey.core.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huddey.core.notification.data.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {}
