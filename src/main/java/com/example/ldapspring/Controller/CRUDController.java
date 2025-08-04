package com.example.ldapspring.Controller;


import com.example.ldapspring.Entity.LdapUser;
import com.example.ldapspring.Service.CRUDService;

import com.example.ldapspring.Service.ReadService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CRUDController {



    private final LdapTemplate ldapTemplate;

    private final CRUDService crudService;

    private final ReadService readService;



    @PostMapping("/create")
    public ResponseEntity<?> createUserDebug(@RequestBody LdapUser user) {
        Map<String, String> result = new HashMap<>();

        try {

            System.out.println("Received user: " + user.toString());
            boolean exists = crudService.userExists(user.getUid());
            result.put("step2_existence_check", exists ? "USER_EXISTS" : "USER_NOT_EXISTS");

            if (exists) {
                result.put("step2_result", "FAILED - User already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }
            result.put("step2_result", "OK");

            // Adım 3: Service create call
            System.out.println("Calling createUser service...");
            LdapUser createdUser = crudService.createUser(user);
            result.put("step3_service_call", "OK");
            result.put("step3_created_uid", createdUser.getUid());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        } catch (Exception e) {
            System.err.println("ERROR in create-debug: " + e.getMessage());
            e.printStackTrace();

            result.put("error", "EXCEPTION");
            result.put("error_message", e.getMessage());
            result.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/deleteuser/{uid}")
    public ResponseEntity<String> deleteUser(@PathVariable String uid) {
        System.out.println("Received user to be deleted: " + uid);
        crudService.deleteUser(uid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/user/{uid}")
    public ResponseEntity<LdapUser> getUser(@PathVariable String uid) {
        System.out.println("Received user to be found: " + uid);
        Optional<LdapUser> user = readService.getUserByUid(uid);

        if (user.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/getall")
    public ResponseEntity<List<LdapUser>> getAllUsers() {
        System.out.println("Received get all users request");
        List<LdapUser> userList = readService.getAllUsers();

        return new  ResponseEntity<>(userList, HttpStatus.OK);
    }

}
