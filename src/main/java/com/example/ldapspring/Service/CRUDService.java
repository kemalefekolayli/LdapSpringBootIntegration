package com.example.ldapspring.Service;

import com.example.ldapspring.Entity.LdapUser;
import com.example.ldapspring.Repositories.LdapUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor
public class CRUDService {


    private final LdapUserRepository ldapUserRepository;
    private final ReadService readService;
    private final LdapTemplate ldapTemplate;





    public LdapUser createUser(LdapUser user) {
        try {
            Optional<LdapUser> existingUser = ldapUserRepository.findByUid(user.getUid());
            if (existingUser.isPresent()) {
                throw new Exception("User already exists");
            }

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword("defaultpass123");
            }

            user.setDn(null);

            return ldapUserRepository.save(user);

        } catch (Exception e) {
            System.err.println("createUser error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Kullanıcı oluşturulamadı: " + e.getMessage(), e);
        }
    }

    private boolean updateUser(){
    return true;
    }

    public void deleteUser(String uid) {
        try {
            Optional<LdapUser> user = ldapUserRepository.findByUid(uid);
            if (user.isPresent()) {
                ldapUserRepository.delete(user.get());
            } else {
                throw new RuntimeException("Kullanıcı bulunamadı: " + uid);
            }
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı silinemedi: " + e.getMessage(), e);
        }
    }

    public boolean userExists(String uid) {
        return readService.getUserByUid(uid).isPresent();
    }



}
