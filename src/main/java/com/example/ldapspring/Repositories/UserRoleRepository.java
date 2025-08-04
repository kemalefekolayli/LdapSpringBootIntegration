package com.example.ldapspring.Repositories;

import com.example.ldapspring.Entity.Auth.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByUser_LdapUidAndRole_Name(String ldapUid, String roleName);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.ldapUid = :ldapUid AND ur.isActive = true " +
            "AND (ur.expiresAt IS NULL OR ur.expiresAt > :now)")
    List<UserRole> findActiveRolesByUser(@Param("ldapUid") String ldapUid, @Param("now") LocalDateTime now);

    @Query("SELECT ur FROM UserRole ur WHERE ur.role.name = :roleName AND ur.isActive = true " +
            "AND (ur.expiresAt IS NULL OR ur.expiresAt > :now)")
    List<UserRole> findActiveUsersByRole(@Param("roleName") String roleName, @Param("now") LocalDateTime now);

    @Query("SELECT ur FROM UserRole ur WHERE ur.expiresAt IS NOT NULL AND ur.expiresAt <= :now AND ur.isActive = true")
    List<UserRole> findExpiredRoles(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.user.ldapUid = :ldapUid AND ur.role.name = :roleName " +
            "AND ur.isActive = true AND (ur.expiresAt IS NULL OR ur.expiresAt > :now)")
    boolean hasActiveRole(@Param("ldapUid") String ldapUid, @Param("roleName") String roleName,
                          @Param("now") LocalDateTime now);

    List<UserRole> findByAssignedBy(String assignedBy);

    List<UserRole> findByUser_LdapUidOrderByAssignedAtDesc(String ldapUid);
}