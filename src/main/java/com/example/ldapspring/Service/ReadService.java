package com.example.ldapspring.Service;


import com.example.ldapspring.Entity.LdapUser;
import com.example.ldapspring.Repositories.LdapUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class ReadService {

    private final LdapUserRepository ldapUserRepository;


    private final LdapTemplate ldapTemplate;


    public List<LdapUser> getAllUsers(){
        return ldapUserRepository.findAll();
    }

    public Optional<LdapUser> getUserByUid(String uid) {
        try {
            Optional<LdapUser> user = ldapUserRepository.findByUid(uid);
            if (user.isPresent()) {
                LdapUser ldapUser = user.get();
            }
            return user;
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<LdapUser> getUserByEmail(String email) {
        try {
            LdapUser user = ldapUserRepository.findByEmail(email);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
