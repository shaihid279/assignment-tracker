package com.tracker.repository;

import com.tracker.model.ApprovalStatus;
import com.tracker.model.Role;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRollNumberIgnoreCase(String rollNumber);

    boolean existsByIdCardNumberIgnoreCase(String idCardNumber);

    long countByRole(Role role);

    long countByRoleAndApprovalStatus(Role role, ApprovalStatus status);

    List<User> findByRoleOrderByFullNameAsc(Role role);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    List<User> findByRoleAndApprovalStatusOrderByFullNameAsc(Role role, ApprovalStatus status);

    List<User> findByRoleAndApprovalStatusOrderByCreatedAtAsc(Role role, ApprovalStatus status);

    @Query("""
            select u from User u
            where u.role = :role
              and (lower(u.fullName) like lower(concat('%', :q, '%'))
                or lower(u.rollNumber) like lower(concat('%', :q, '%'))
                or lower(u.idCardNumber) like lower(concat('%', :q, '%')))
            order by u.fullName asc
            """)
    List<User> searchByRole(@Param("role") Role role, @Param("q") String q);
}