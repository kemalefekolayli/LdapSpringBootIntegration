package com.example.ldapspring.Service;


import com.example.ldapspring.Entity.Auth.User;
import com.example.ldapspring.Repositories.UserRepository;
import com.example.ldapspring.Entity.LdapUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // LDAP'tan gelen kullanıcıyı PostgreSQL'e sync etme
    public User syncUserFromLdap(LdapUser ldapUser) {
        Optional<User> existingUser = userRepository.findByLdapUid(ldapUser.getUid());

        if (existingUser.isPresent()) {
            // Mevcut kullanıcıyı güncelle
            User user = existingUser.get();
            user.setEmail(ldapUser.getEmail());
            user.setFullName(ldapUser.getFullName());
            user.setIsActive(true);
            return userRepository.save(user);
        } else {
            // Yeni kullanıcı oluştur
            User newUser = new User(
                    ldapUser.getUid(),
                    ldapUser.getEmail(),
                    ldapUser.getFullName()
            );
            return userRepository.save(newUser);
        }
    }

    // LDAP uid ile kullanıcı bulma
    public Optional<User> findByLdapUid(String ldapUid) {
        return userRepository.findByLdapUid(ldapUid);
    }

    // Kullanıcı oluşturma (manuel)
    public User createUser(String ldapUid, String email, String fullName) {
        if (userRepository.existsByLdapUid(ldapUid)) {
            throw new RuntimeException("User already exists with ldapUid: " + ldapUid);
        }

        User user = new User(ldapUid, email, fullName);
        return userRepository.save(user);
    }

    // Kullanıcı güncelleme
    public User updateUser(Long userId, String email, String fullName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setEmail(email);
        user.setFullName(fullName);
        return userRepository.save(user);
    }

    // Kullanıcıyı deaktif etme (silmek yerine)
    public void deactivateUser(String ldapUid) {
        User user = userRepository.findByLdapUid(ldapUid)
                .orElseThrow(() -> new RuntimeException("User not found with ldapUid: " + ldapUid));

        user.setIsActive(false);
        userRepository.save(user);
    }

    // Tüm aktif kullanıcıları listeleme
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    // Kullanıcı arama
    public List<User> searchUsers(String fullName) {
        return userRepository.findByFullNameContainingIgnoreCase(fullName);
    }

    // Kullanıcının var olup olmadığını kontrol etme
    public boolean existsByLdapUid(String ldapUid) {
        return userRepository.existsByLdapUid(ldapUid);
    }
}