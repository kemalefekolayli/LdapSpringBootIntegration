package com.example.ldapspring.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private LdapTemplate ldapTemplate;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Controller çalışıyor");
    }

    @GetMapping("/ldap-context")
    public ResponseEntity<Map<String, String>> checkLdapContext() {
        Map<String, String> result = new HashMap<>();

        try {
            String contextInfo = ldapTemplate.getContextSource().toString();
            result.put("status", "SUCCESS");
            result.put("context", contextInfo);
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-base")
    public ResponseEntity<Map<String, String>> testBaseDN() {
        Map<String, String> result = new HashMap<>();

        try {
            Object lookup = ldapTemplate.lookup("dc=example,dc=org");
            result.put("base_dn", "EXISTS");
            result.put("object_type", lookup.getClass().getSimpleName());
        } catch (Exception e) {
            result.put("base_dn", "FAILED");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-people")
    public ResponseEntity<Map<String, String>> testPeopleOU() {
        Map<String, String> result = new HashMap<>();

        try {
            Object lookup = ldapTemplate.lookup("ou=people,dc=example,dc=org");
            result.put("people_ou", "EXISTS");
        } catch (Exception e) {
            result.put("people_ou", "NOT_FOUND");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-all-ous")
    public ResponseEntity<Map<String, String>> testAllOUs() {
        Map<String, String> result = new HashMap<>();

        String[] ous = {"people", "IT", "Marketing", "groups"};

        for (String ou : ous) {
            try {
                ldapTemplate.lookup("ou=" + ou + ",dc=example,dc=org");
                result.put(ou, "EXISTS");
            } catch (Exception e) {
                result.put(ou, "NOT_FOUND");
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/connection-info")
    public ResponseEntity<Map<String, String>> connectionInfo() {
        Map<String, String> result = new HashMap<>();

        result.put("expected_url", "ldap://localhost:389");
        result.put("expected_base", "dc=example,dc=org");
        result.put("expected_admin", "cn=admin,dc=example,dc=org");

        try {
            String context = ldapTemplate.getContextSource().toString();
            result.put("actual_context", context);
            result.put("context_status", "OK");
        } catch (Exception e) {
            result.put("context_status", "ERROR");
            result.put("context_error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}