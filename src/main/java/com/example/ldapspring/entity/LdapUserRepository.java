package com.example.ldapspring.entity;

import com.example.ldapspring.entity.LdapUser;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LdapUserRepository extends LdapRepository<LdapUser> {

    Optional<LdapUser> findByUid(String uid);


    LdapUser findByEmail(String email);

    LdapUser findByEmailAndPassword(String email, String password );

    List<LdapUser> findByFullNameContainingIgnoreCase(String fullName);

    List<LdapUser> findByLastName(String lastName);


    List<LdapUser> findAll();
}