package com.example.ldapspring.Service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for validating various input values and checking relationships
 * between entities such as user and group membership.
 */
@Service
@AllArgsConstructor
public class InputValidationService {

    private final GroupService groupService;
    private final UserService userService;

    private static final Pattern UID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,50}$");

    /**
     * Validates an LDAP uid using a simple alphanumeric pattern.
     *
     * @param uid uid to validate
     * @return {@code true} if uid matches the pattern
     */
    public boolean isValidUid(String uid) {
        return uid != null && UID_PATTERN.matcher(uid).matches();
    }

    /**
     * Validates email format.
     *
     * @param email email to validate
     * @return {@code true} if email matches the pattern
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates group name format.
     *
     * @param groupName group name to validate
     * @return {@code true} if group name matches the pattern
     */
    public boolean isValidGroupName(String groupName) {
        return groupName != null && GROUP_NAME_PATTERN.matcher(groupName).matches();
    }

    /**
     * Helper method to check if a user belongs to a specific group.
     * Uses {@link UserService} and {@link GroupService} to verify existence
     * and membership.
     *
     * @param uid       LDAP uid of the user
     * @param groupName name of the group
     * @return {@code true} if user exists and is a member of the group
     */
    public boolean isUserMemberOfGroup(String uid, String groupName) {
        if (!isValidUid(uid) || !isValidGroupName(groupName)) {
            return false;
        }

        if (!userService.existsByLdapUid(uid)) {
            return false;
        }

        return groupService.getUserGroups(uid)
                .stream()
                .anyMatch(group -> group.getName().equalsIgnoreCase(groupName));
    }
}
