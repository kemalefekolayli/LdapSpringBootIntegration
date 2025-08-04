package com.example.ldapspring.Service;

import com.example.ldapspring.Entity.Auth.Group;
import com.example.ldapspring.Entity.Auth.Role;
import com.example.ldapspring.Entity.Auth.User;
import com.example.ldapspring.Repositories.GroupRepository;
import com.example.ldapspring.Repositories.RoleRepository;
import com.example.ldapspring.Repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Grup oluşturma
    public Group createGroup(String name, String description) {
        if (groupRepository.existsByName(name)) {
            throw new RuntimeException("Group already exists with name: " + name);
        }

        Group group = new Group(name, description);
        return groupRepository.save(group);
    }

    // Grup güncelleme
    public Group updateGroup(Long groupId, String name, String description) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        // Eğer isim değişecekse, başka grup bu ismi kullanıyor mu kontrolü
        if (!group.getName().equals(name) && groupRepository.existsByName(name)) {
            throw new RuntimeException("Group already exists with name: " + name);
        }

        group.setName(name);
        group.setDescription(description);
        return groupRepository.save(group);
    }

    // Grubu deaktif etme
    public void deactivateGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        group.setIsActive(false);
        groupRepository.save(group);
    }

    // Grup adı ile bulma
    public Optional<Group> findByName(String name) {
        return groupRepository.findByName(name);
    }

    // ID ile bulma
    public Optional<Group> findById(Long groupId) {
        return groupRepository.findById(groupId);
    }

    // Tüm aktif grupları listeleme
    public List<Group> getAllActiveGroups() {
        return groupRepository.findByIsActiveTrue();
    }

    // Kullanıcının gruplarını bulma
    public List<Group> getUserGroups(String ldapUid) {
        return groupRepository.findByUserLdapUid(ldapUid);
    }

    // Kullanıcıyı gruba ekleme
    public void addUserToGroup(String ldapUid, Long groupId) {
        User user = userRepository.findByLdapUid(ldapUid)
                .orElseThrow(() -> new RuntimeException("User not found with ldapUid: " + ldapUid));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        if (!group.getIsActive()) {
            throw new RuntimeException("Cannot add user to inactive group");
        }

        group.getUsers().add(user);
        groupRepository.save(group);
    }

    // Kullanıcıyı gruptan çıkarma
    public void removeUserFromGroup(String ldapUid, Long groupId) {
        User user = userRepository.findByLdapUid(ldapUid)
                .orElseThrow(() -> new RuntimeException("User not found with ldapUid: " + ldapUid));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        group.getUsers().remove(user);
        groupRepository.save(group);
    }

    // Gruba rol atama
    public void assignRoleToGroup(Long groupId, Long roleId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (!group.getIsActive() || !role.getIsActive()) {
            throw new RuntimeException("Cannot assign inactive role to inactive group");
        }

        group.getRoles().add(role);
        groupRepository.save(group);
    }

    // Gruptan rol çıkarma
    public void removeRoleFromGroup(Long groupId, Long roleId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        group.getRoles().remove(role);
        groupRepository.save(group);
    }

    // Grup arama
    public List<Group> searchGroups(String name) {
        return groupRepository.findByNameContainingIgnoreCase(name);
    }
}