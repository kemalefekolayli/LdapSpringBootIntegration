package com.example.ldapspring.Service;


import com.example.ldapspring.Entity.Auth.Role;
import com.example.ldapspring.Repositories.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    // Rol oluşturma
    public Role createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("Role already exists with name: " + name);
        }

        Role role = new Role(name, description);
        return roleRepository.save(role);
    }

    // Rol güncelleme
    public Role updateRole(Long roleId, String name, String description) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));


        if (!role.getName().equals(name) && roleRepository.existsByName(name)) {
            throw new RuntimeException("Role already exists with name: " + name);
        }

        role.setName(name);
        role.setDescription(description);
        return roleRepository.save(role);
    }


    public void deactivateRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        role.setIsActive(false);
        roleRepository.save(role);
    }


    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }


    public Optional<Role> findById(Long roleId) {
        return roleRepository.findById(roleId);
    }


    public List<Role> getAllActiveRoles() {
        return roleRepository.findByIsActiveTrue();
    }


    public List<Role> getUserRoles(String ldapUid) {
        return roleRepository.findByUserLdapUid(ldapUid);
    }


    public List<Role> searchRoles(String name) {
        return roleRepository.findByNameContainingIgnoreCase(name);
    }
}