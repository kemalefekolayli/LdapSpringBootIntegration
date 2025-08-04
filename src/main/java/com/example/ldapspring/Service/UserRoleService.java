package com.example.ldapspring.Service;

import com.example.ldapspring.Entity.Auth.Role;
import com.example.ldapspring.Entity.Auth.User;
import com.example.ldapspring.Entity.Auth.UserRole;
import com.example.ldapspring.Repositories.RoleRepository;
import com.example.ldapspring.Repositories.UserRepository;
import com.example.ldapspring.Repositories.UserRoleRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Kullanıcıya rol atama
    public UserRole assignRoleToUser(String ldapUid, String roleName, String assignedBy) {
        User user = userRepository.findByLdapUid(ldapUid)
                .orElseThrow(() -> new RuntimeException("User not found with ldapUid: " + ldapUid));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + roleName));

        if (!user.getIsActive() || !role.getIsActive()) {
            throw new RuntimeException("Cannot assign inactive role to inactive user");
        }

        // Zaten bu rol atanmış mı kontrol et
        Optional<UserRole> existingUserRole = userRoleRepository
                .findByUser_LdapUidAndRole_Name(ldapUid, roleName);

        if (existingUserRole.isPresent() && existingUserRole.get().getIsActive()) {
            throw new RuntimeException("User already has this role");
        }

        UserRole userRole = new UserRole(user, role, assignedBy);
        return userRoleRepository.save(userRole);
    }

    // Geçici rol atama (süreli)
    public UserRole assignTemporaryRole(String ldapUid, String roleName, String assignedBy, LocalDateTime expiresAt) {
        User user = userRepository.findByLdapUid(ldapUid)
                .orElseThrow(() -> new RuntimeException("User not found with ldapUid: " + ldapUid));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + roleName));

        if (!user.getIsActive() || !role.getIsActive()) {
            throw new RuntimeException("Cannot assign inactive role to inactive user");
        }

        UserRole userRole = new UserRole(user, role, assignedBy, expiresAt);
        return userRoleRepository.save(userRole);
    }

    // Kullanıcıdan rol çıkarma
    public void removeRoleFromUser(String ldapUid, String roleName) {
        Optional<UserRole> userRole = userRoleRepository
                .findByUser_LdapUidAndRole_Name(ldapUid, roleName);

        if (userRole.isPresent()) {
            UserRole ur = userRole.get();
            ur.setIsActive(false);
            userRoleRepository.save(ur);
        } else {
            throw new RuntimeException("User does not have this role");
        }
    }

    // Kullanıcının aktif rollerini getirme
    public List<UserRole> getUserActiveRoles(String ldapUid) {
        return userRoleRepository.findActiveRolesByUser(ldapUid, LocalDateTime.now());
    }

    // Kullanıcının belirli rolü var mı kontrol etme
    public boolean hasRole(String ldapUid, String roleName) {
        return userRoleRepository.hasActiveRole(ldapUid, roleName, LocalDateTime.now());
    }

    // Süresi dolmuş rolleri temizleme (scheduled task için)
    public List<UserRole> cleanupExpiredRoles() {
        List<UserRole> expiredRoles = userRoleRepository.findExpiredRoles(LocalDateTime.now());

        for (UserRole expiredRole : expiredRoles) {
            expiredRole.setIsActive(false);
        }

        userRoleRepository.saveAll(expiredRoles);
        return expiredRoles;
    }

    // Kullanıcının tüm rol geçmişini getirme
    public List<UserRole> getUserRoleHistory(String ldapUid) {
        return userRoleRepository.findByUser_LdapUidOrderByAssignedAtDesc(ldapUid);
    }

    // Belirli rol sahibi kullanıcıları getirme
    public List<UserRole> getUsersWithRole(String roleName) {
        return userRoleRepository.findActiveUsersByRole(roleName, LocalDateTime.now());
    }
}