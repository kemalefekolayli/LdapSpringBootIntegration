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


    public Group createGroup(String name, String description) {
        if (groupRepository.existsByName(name)) {
            throw new RuntimeException("Group already exists with name: " + name);
        }

        Group group = new Group(name, description);
        return groupRepository.save(group);
    }


    public Group updateGroup(Long groupId, String name, String description) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));


        if (!group.getName().equals(name) && groupRepository.existsByName(name)) {
            throw new RuntimeException("Group already exists with name: " + name);
        }

        group.setName(name);
        group.setDescription(description);
        return groupRepository.save(group);
    }


    public void deactivateGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        group.setIsActive(false);
        groupRepository.save(group);
    }


    public Optional<Group> findByName(String name) {
        return groupRepository.findByName(name);
    }


    public Optional<Group> findById(Long groupId) {
        return groupRepository.findById(groupId);
    }

    public List<Group> getAllActiveGroups() {
        return groupRepository.findByIsActiveTrue();
    }

    public List<Group> getUserGroups(String ldapUid) {
        return groupRepository.findByUserLdapUid(ldapUid);
    }

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

    public void removeUserFromGroup(String ldapUid, Long groupId) {
        User user = userRepository.findByLdapUid(ldapUid)
                .orElseThrow(() -> new RuntimeException("User not found with ldapUid: " + ldapUid));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        group.getUsers().remove(user);
        groupRepository.save(group);
    }

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

    public void removeRoleFromGroup(Long groupId, Long roleId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        group.getRoles().remove(role);
        groupRepository.save(group);
    }

    public List<Group> searchGroups(String name) {
        return groupRepository.findByNameContainingIgnoreCase(name);
    }
}