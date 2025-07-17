package com.example.ldapspring.entity;

import com.example.ldapspring.entity.ldapUser;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILdapUserRepository extends LdapRepository<ldapUser> {
    ldapUser findByUid(String uid);
}
