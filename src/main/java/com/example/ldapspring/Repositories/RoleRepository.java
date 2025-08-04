package com.example.ldapspring.Repositories;


import com.example.ldapspring.Entity.Auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


    Optional<Role> findByName(String name);

    List<Role> findByIsActiveTrue();

    boolean existsByName(String name);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.userRoles ur WHERE ur.user.ldapUid = :ldapUid AND ur.isActive = true")
    List<Role> findByUserLdapUid(@Param("ldapUid") String ldapUid);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.groups g WHERE g.name = :groupName AND g.isActive = true")
    List<Role> findByGroupName(@Param("groupName") String groupName);

    List<Role> findByNameContainingIgnoreCase(String name);
}