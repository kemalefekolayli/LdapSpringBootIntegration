package com.example.ldapspring.Repositories;

import com.example.ldapspring.Entity.Auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLdapUid(String ldapUid);

    Optional<User> findByEmail(String email);

    List<User> findByIsActiveTrue();

    boolean existsByLdapUid(String ldapUid);

    List<User> findByFullNameContainingIgnoreCase(String fullName);

    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur WHERE ur.role.name = :roleName AND ur.isActive = true")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT DISTINCT u FROM User u JOIN u.groups g WHERE g.name = :groupName AND g.isActive = true")
    List<User> findByGroupName(@Param("groupName") String groupName);
}
