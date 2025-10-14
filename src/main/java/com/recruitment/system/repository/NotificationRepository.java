package com.recruitment.system.repository;

import com.recruitment.system.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, Boolean read, Pageable pageable);

    long countByUserIdAndRead(Long userId, Boolean isRead);

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE notifications SET is_read = true, read_at = :readAt WHERE user_id = :userId AND is_read = false",
            nativeQuery = true
    )
    int markAllRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}



