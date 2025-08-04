package com.example.ldapspring.Service;

import com.example.ldapspring.Entity.Auth.Role;
import com.example.ldapspring.Entity.Auth.Group;
import com.example.ldapspring.Entity.Auth.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationService {

    private final UserRoleService userRoleService;
    private final GroupService groupService;
    private final RoleService roleService;

    // Kullanıcının tüm rollerini getirme (direkt + grup üzerinden)
    public Set<String> getAllUserRoles(String ldapUid) {
        Set<String> allRoles = new HashSet<>();

        // 1. Direkt atanan roller
        List<UserRole> directRoles = userRoleService.getUserActiveRoles(ldapUid);
        Set<String> directRoleNames = directRoles.stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
        allRoles.addAll(directRoleNames);

        // 2. Grup üzerinden gelen roller
        List<Group> userGroups = groupService.getUserGroups(ldapUid);
        for (Group group : userGroups) {
            if (group.getIsActive()) {
                Set<String> groupRoleNames = group.getRoles().stream()
                        .filter(role -> role.getIsActive())
                        .map(Role::getName)
                        .collect(Collectors.toSet());
                allRoles.addAll(groupRoleNames);
            }
        }

        return allRoles;
    }

    // Kullanıcının belirli rolü var mı kontrol etme (direkt + grup)
    public boolean hasRole(String ldapUid, String roleName) {
        // 1. Direkt rol kontrolü
        if (userRoleService.hasRole(ldapUid, roleName)) {
            return true;
        }

        // 2. Grup üzerinden rol kontrolü
        List<Group> userGroups = groupService.getUserGroups(ldapUid);
        for (Group group : userGroups) {
            if (group.getIsActive()) {
                boolean hasRoleInGroup = group.getRoles().stream()
                        .anyMatch(role -> role.getIsActive() && role.getName().equals(roleName));
                if (hasRoleInGroup) {
                    return true;
                }
            }
        }

        return false;
    }

    // Kullanıcının herhangi bir rolü var mı kontrol etme
    public boolean hasAnyRole(String ldapUid, String... roleNames) {
        for (String roleName : roleNames) {
            if (hasRole(ldapUid, roleName)) {
                return true;
            }
        }
        return false;
    }

    // Kullanıcının tüm gruplarını getirme
    public List<Group> getUserGroups(String ldapUid) {
        return groupService.getUserGroups(ldapUid);
    }

    // Kullanıcının yetkilendirme özeti
    public UserAuthorizationSummary getUserAuthorizationSummary(String ldapUid) {
        Set<String> allRoles = getAllUserRoles(ldapUid);
        List<Group> userGroups = getUserGroups(ldapUid);
        List<UserRole> directRoles = userRoleService.getUserActiveRoles(ldapUid);

        return new UserAuthorizationSummary(
                ldapUid,
                allRoles,
                userGroups.stream().map(Group::getName).collect(Collectors.toSet()),
                directRoles.stream().map(ur -> ur.getRole().getName()).collect(Collectors.toSet())
        );
    }

    // Inner class - Kullanıcı yetkilendirme özeti
    public static class UserAuthorizationSummary {
        private final String ldapUid;
        private final Set<String> allRoles;
        private final Set<String> groups;
        private final Set<String> directRoles;

        public UserAuthorizationSummary(String ldapUid, Set<String> allRoles,
                                        Set<String> groups, Set<String> directRoles) {
            this.ldapUid = ldapUid;
            this.allRoles = allRoles;
            this.groups = groups;
            this.directRoles = directRoles;
        }

        // Getters
        public String getLdapUid() { return ldapUid; }
        public Set<String> getAllRoles() { return allRoles; }
        public Set<String> getGroups() { return groups; }
        public Set<String> getDirectRoles() { return directRoles; }
        public Set<String> getInheritedRoles() {
            Set<String> inherited = new HashSet<>(allRoles);
            inherited.removeAll(directRoles);
            return inherited;
        }
    }
}