package com.example.ldapspring.Controller;

import com.example.ldapspring.entity.LdapUser;
import com.example.ldapspring.service.LdapUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class LdapUserController {

    @Autowired
    private LdapUserService ldapUserService;

    @GetMapping("/deneme")
    public ResponseEntity<String> isUp(){
        return new ResponseEntity<>("UP", HttpStatus.OK);
    }

    @PostMapping("/getbyemailandpassword")
    public ResponseEntity<LdapUser> getUserByEmailAndPassword(@RequestBody EmailAndPassword emailAndPassword) {
        try {
            String email = emailAndPassword.getEmail();
            String password = emailAndPassword.getPassword();
            Optional<LdapUser> user = ldapUserService.getUserByEmailAndPassword(email, password);
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Tüm kullanıcıları getir
     */
    @GetMapping("/getall")
    public ResponseE ntity<List<LdapUser>> getAllUsers() {
        try {
            List<LdapUser> users = ldapUserService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * UID ile kullanıcı getir
     */
    @GetMapping("/uid/{uid}")
    public ResponseEntity<LdapUser> getUserByUid(@PathVariable String uid) {
        try {
            Optional<LdapUser> user = ldapUserService.getUserByUid(uid);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Email ile kullanıcı ara
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<LdapUser> getUserByEmail(@PathVariable String email) {
        try {
            Optional<LdapUser> user = ldapUserService.getUserByEmail(email);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * İsme göre kullanıcı ara
     */
    @GetMapping("/search")
    public ResponseEntity<List<LdapUser>> searchUsers(@RequestParam String name) {
        try {
            List<LdapUser> users = ldapUserService.searchUsersByName(name);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Yeni kullanıcı oluştur
     */
    @PostMapping
    public ResponseEntity<LdapUser> createUser(@RequestBody LdapUser user) {
        try {
            // Kullanıcının zaten var olup olmadığını kontrol et
            if (ldapUserService.userExists(user.getUid())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            LdapUser createdUser = ldapUserService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Kullanıcı güncelle
     */
    @PutMapping("/{uid}")
    public ResponseEntity<LdapUser> updateUser(@PathVariable String uid, @RequestBody LdapUser user) {
        try {
            // Kullanıcının var olup olmadığını kontrol et
            if (!ldapUserService.userExists(uid)) {
                return ResponseEntity.notFound().build();
            }

            user.setUid(uid); // UID'nin değişmemesini sağla
            LdapUser updatedUser = ldapUserService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Kullanıcı sil
     */
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> deleteUser(@PathVariable String uid) {
        try {
            if (!ldapUserService.userExists(uid)) {
                return ResponseEntity.notFound().build();
            }

            ldapUserService.deleteUser(uid);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Kullanıcı şifresi güncelle
     */
    @PatchMapping("/{uid}/password")
    public ResponseEntity<Void> updateUserPassword(@PathVariable String uid, @RequestBody PasswordUpdateRequest request) {
        try {
            ldapUserService.updateUserPassword(uid, request.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * LDAP bağlantı durumu kontrolü
     */
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> checkHealth() {
        boolean isHealthy = ldapUserService.checkConnection();
        HealthStatus status = new HealthStatus(isHealthy ? "UP" : "DOWN", isHealthy);
        return ResponseEntity.ok(status);
    }

    // Inner classes for request/response objects
    public static class PasswordUpdateRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class EmailAndPassword {
        private String email;
        private String password;

        public String getEmail(){
            return email;
        }
        public String getPassword(){
            return password;
        }
    }

    public static class HealthStatus {
        private String status;
        private boolean connected;

        public HealthStatus(String status, boolean connected) {
            this.status = status;
            this.connected = connected;
        }

        public String getStatus() {
            return status;
        }

        public boolean isConnected() {
            return connected;
        }
    }
}