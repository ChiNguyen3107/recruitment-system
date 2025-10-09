package com.recruitment.system.repository;

import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    List<User> findByCompanyId(Long companyId);

    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.company.id = :companyId")
    List<User> findByRoleAndCompanyId(@Param("role") UserRole role, @Param("companyId") Long companyId);

    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetExpires > :now")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    Long countNewUsersFromDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT u FROM User u WHERE u.lastLogin < :date OR u.lastLogin IS NULL")
    List<User> findInactiveUsersSince(@Param("date") LocalDateTime date);

    // ===================== Native queries cho admin analytics =====================

    // Active users (login trong 30 ngày)
    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.last_login >= DATE_SUB(NOW(), INTERVAL 30 DAY)", nativeQuery = true)
    Long countActiveUsersLast30Days();

    // New users this month
    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.created_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01')", nativeQuery = true)
    Long countNewUsersThisMonth();

    // User growth 12 tháng gần nhất
    @Query(value = "SELECT YEAR(u.created_at) AS y, MONTH(u.created_at) AS m, COUNT(*) AS c FROM users u \n" +
            "WHERE u.created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) GROUP BY y, m ORDER BY y, m", nativeQuery = true)
    List<Object[]> userGrowthLast12Months();

    @Query(value = "SELECT YEAR(u.created_at) AS y, MONTH(u.created_at) AS m, COUNT(*) AS c FROM users u \n" +
            "WHERE (:from IS NULL OR u.created_at >= :from) AND (:to IS NULL OR u.created_at <= :to) \n" +
            "GROUP BY y, m ORDER BY y, m", nativeQuery = true)
    List<Object[]> userGrowthInRange(@Param("from") java.time.LocalDateTime from,
                                    @Param("to") java.time.LocalDateTime to);
}