package com.example.ldapspring.Repositories;


import com.example.ldapspring.Entity.Auth.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {


    Optional<Group> findByName(String name);


    List<Group> findByIsActiveTrue();


    boolean existsByName(String name);


    @Query("SELECT DISTINCT g FROM Group g JOIN g.users u WHERE u.ldapUid = :ldapUid AND g.isActive = true")
    List<Group> findByUserLdapUid(@Param("ldapUid") String ldapUid);

    @Query("SELECT DISTINCT g FROM Group g JOIN g.roles r WHERE r.name = :roleName AND g.isActive = true")
    List<Group> findByRoleName(@Param("roleName") String roleName);

    List<Group> findByNameContainingIgnoreCase(String name);
}