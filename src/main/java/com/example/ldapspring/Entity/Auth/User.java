package com.example.ldapspring.Entity.Auth;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ldap_uid", unique = true, nullable = false)
    private String ldapUid; // LDAP'taki uid ile eşleşecek

    @Column(name = "email")
    private String email; // Cache için, LDAP'tan sync edilecek

    @Column(name = "full_name")
    private String fullName; // Cache için, LDAP'tan sync edilecek

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // İlişkiler
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private Set<Group> groups = new HashSet<>();

    // Constructor
    public User(String ldapUid, String email, String fullName) {
        this.ldapUid = ldapUid;
        this.email = email;
        this.fullName = fullName;
    }
}