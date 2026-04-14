package com.badminton.booking.domain.repository;

import com.badminton.booking.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT u FROM User u WHERE u.isDeleted = false ORDER BY u.id DESC")
    List<User> findAllActiveUsersWithRole();

    @EntityGraph(attributePaths = {"role"})
    @Query("""
            SELECT u FROM User u
            WHERE u.isDeleted = false
              AND (
                  LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY u.id DESC
            """)
    List<User> searchUsersWithRole(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"role", "managedBranch"})
    List<User> findByRoleNameAndIsDeletedFalse(String roleName);

    @EntityGraph(attributePaths = {"role", "managedBranch"})
    List<User> findByRoleNameAndIsDeletedFalseAndManagedBranchIsNull(String roleName);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.managedBranch.id = :branchId")
    List<User> findAllByIsDeletedFalseAndManagedBranchId(@Param("branchId") Long branchId);
}
