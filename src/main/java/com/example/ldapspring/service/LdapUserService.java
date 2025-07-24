package com.example.ldapspring.service;

import com.example.ldapspring.entity.LdapUser;
import com.example.ldapspring.entity.LdapUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import java.util.List;
import java.util.Optional;

@Service
public class LdapUserService {

    @Autowired
    private LdapUserRepository ldapUserRepository;

    @Autowired
    private LdapTemplate ldapTemplate;


    public List<LdapUser> getAllUsers() {
        return ldapUserRepository.findAll();
    }


    public Optional<LdapUser> getUserByUid(String uid) {
        try {
            LdapUser user = ldapUserRepository.findByUid(uid);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<LdapUser> getUserByEmailAndPassword(String email, String password) {
        try {
            LdapUser user = ldapUserRepository.findByEmailAndPassword(email, password);
            return Optional.ofNullable(user);
        }
        catch (Exception e) {
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


    public List<LdapUser> searchUsersByName(String name) {
        return ldapUserRepository.findByFullNameContainingIgnoreCase(name);
    }




    public LdapUser createUser(LdapUser user) {
        try {
            // Input validation
            if (user.getUid() == null || user.getUid().trim().isEmpty()) {
                throw new IllegalArgumentException("UID boş olamaz");
            }
            if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                throw new IllegalArgumentException("Full name boş olamaz");
            }
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                throw new IllegalArgumentException("Last name boş olamaz");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email boş olamaz");
            }

            // Kullanıcının zaten var olup olmadığını kontrol et
            LdapUser existingUser = null;
            try {
                existingUser = ldapUserRepository.findByUid(user.getUid());
            } catch (Exception e) {
                // Repository error ignore - user doesn't exist
            }

            if (existingUser != null) {
                throw new RuntimeException("Bu UID ile kullanıcı zaten mevcut: " + user.getUid());
            }

            // Default password set et if empty
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword("defaultpass123");
            }

            user.setDn(null);

            System.out.println("Creating user with auto-generated DN: " + user.getUid());
            System.out.println("User details: " + user.toString());

            return ldapUserRepository.save(user);

        } catch (Exception e) {
            System.err.println("createUser error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Kullanıcı oluşturulamadı: " + e.getMessage(), e);
        }
    }

    public LdapUser updateUser(LdapUser user) {
        try {
            return ldapUserRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı güncellenemedi: " + e.getMessage(), e);
        }
    }


    public void deleteUser(String uid) {
        try {
            LdapUser user = ldapUserRepository.findByUid(uid);
            if (user != null) {
                ldapUserRepository.delete(user);
            } else {
                throw new RuntimeException("Kullanıcı bulunamadı: " + uid);
            }
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı silinemedi: " + e.getMessage(), e);
        }
    }


    public void updateUserPassword(String uid, String newPassword) {
        try {
            Optional<LdapUser> userOpt = getUserByUid(uid);
            if (userOpt.isPresent()) {
                LdapUser user = userOpt.get();
                user.setPassword(newPassword);
                ldapUserRepository.save(user);
            } else {
                throw new RuntimeException("Kullanıcı bulunamadı: " + uid);
            }
        } catch (Exception e) {
            throw new RuntimeException("Şifre güncellenemedi: " + e.getMessage(), e);
        }
    }

    public boolean userExists(String uid) {
        return getUserByUid(uid).isPresent();
    }


    public boolean checkConnection() {
        try {
            // Basit bir LDAP sorgusu ile bağlantıyı test et
            ldapTemplate.lookup("ou=people,dc=example,dc=org");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}