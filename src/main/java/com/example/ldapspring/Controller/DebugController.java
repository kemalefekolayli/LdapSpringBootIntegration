package com.example.ldapspring.Controller;

import com.example.ldapspring.Service.CRUDService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.bind.annotation.*;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@AllArgsConstructor
public class DebugController {



    private final LdapTemplate ldapTemplate;

    private final CRUDService crudService;

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
            // Search ile test et - açık tip belirtimi
            List<String> searchResult = ldapTemplate.search(
                    "",  // base DN'den itibaren ara
                    "(objectClass=dcObject)",  // dcObject olan entry'leri ara
                    new AttributesMapper<String>() {
                        @Override
                        public String mapFromAttributes(Attributes attributes) throws NamingException {
                            return attributes.toString();
                        }
                    }
            );


            result.put("base_dn", "EXISTS");
            result.put("search_result_count", String.valueOf(searchResult.size()));
            result.put("method", "search");

            if (!searchResult.isEmpty()) {
                result.put("first_result", searchResult.get(0));
            }

        } catch (Exception e) {
            result.put("base_dn", "SEARCH_FAILED");
            result.put("error", e.getMessage());

            // Alternatif: lookup dene
            try {
                Object lookup = ldapTemplate.lookup("dc=example,dc=org");
                result.put("lookup_attempt", "SUCCESS");
                result.put("lookup_result", lookup != null ? lookup.toString() : "null");
            } catch (Exception lookupEx) {
                result.put("lookup_attempt", "FAILED");
                result.put("lookup_error", lookupEx.getMessage());
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-people")
    public ResponseEntity<Map<String, String>> testPeopleOU() {
        Map<String, String> result = new HashMap<>();

        try {
            // Search ile people OU'yu test et - açık tip belirtimi
            List<String> searchResult = ldapTemplate.search(
                    "ou=people",  // people OU'da ara
                    "(objectClass=organizationalUnit)",  // organizationalUnit olan entry'leri ara
                    new AttributesMapper<String>() {
                        @Override
                        public String mapFromAttributes(Attributes attributes) throws NamingException {
                            return attributes.toString();
                        }
                    }
            );

            result.put("people_ou", "EXISTS");
            result.put("search_result_count", String.valueOf(searchResult.size()));
            result.put("method", "search");

        } catch (Exception e) {
            result.put("people_ou", "SEARCH_FAILED");
            result.put("search_error", e.getMessage());

            // Alternatif: lookup dene
            try {
                Object lookup = ldapTemplate.lookup("ou=people,dc=example,dc=org");
                result.put("lookup_attempt", "SUCCESS");
                result.put("lookup_result", lookup != null ? lookup.toString() : "null");
            } catch (Exception lookupEx) {
                result.put("lookup_attempt", "FAILED");
                result.put("lookup_error", lookupEx.getMessage());
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-all-ous")
    public ResponseEntity<Map<String, String>> testAllOUs() {
        Map<String, String> result = new HashMap<>();

        String[] ous = {"people", "IT", "Marketing", "groups"};

        for (String ou : ous) {
            try {
                // Search ile test et - açık tip belirtimi
                List<String> searchResult = ldapTemplate.search(
                        "ou=" + ou,
                        "(objectClass=organizationalUnit)",
                        new AttributesMapper<String>() {
                            @Override
                            public String mapFromAttributes(Attributes attributes) throws NamingException {
                                return attributes.toString();
                            }
                        }
                );

                if (!searchResult.isEmpty()) {
                    result.put(ou, "EXISTS_VIA_SEARCH");
                } else {
                    result.put(ou, "EMPTY_SEARCH_RESULT");
                }

            } catch (Exception e) {
                result.put(ou, "SEARCH_FAILED: " + e.getMessage());
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

    @GetMapping("/test-users-in-people")
    public ResponseEntity<Map<String, String>> testUsersInPeople() {
        Map<String, String> result = new HashMap<>();

        try {
            // People OU'da kullanıcı ara - ContextMapper kullan
            List<String> users = ldapTemplate.search(
                    "ou=people",
                    "(objectClass=inetOrgPerson)",
                    new ContextMapper<String>() {
                        @Override
                        public String mapFromContext(Object ctx) throws NamingException {
                            DirContextAdapter context = (DirContextAdapter) ctx;
                            return context.getDn().toString();
                        }
                    }
            );

            result.put("users_found", String.valueOf(users.size()));
            result.put("status", "SUCCESS");

            for (int i = 0; i < Math.min(3, users.size()); i++) {
                result.put("user_" + (i+1), users.get(i));
            }

        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/ldap-structure")
    public ResponseEntity<Map<String, Object>> exploreLdapStructure() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Root level'dan başlayarak yapıyı keşfet - ContextMapper kullan
            List<String> rootEntries = ldapTemplate.search(
                    "",
                    "(objectClass=*)",
                    new ContextMapper<String>() {
                        @Override
                        public String mapFromContext(Object ctx) throws NamingException {
                            DirContextAdapter context = (DirContextAdapter) ctx;
                            return context.getDn().toString();
                        }
                    }
            );

            result.put("total_entries_from_root", rootEntries.size());
            result.put("first_10_entries", rootEntries.subList(0, Math.min(10, rootEntries.size())));
            result.put("status", "SUCCESS");

        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/simple-base-test")
    public ResponseEntity<Map<String, String>> simpleBaseTest() {
        Map<String, String> result = new HashMap<>();

        try {
            // En basit test: sadece lookup
            Object lookup = ldapTemplate.lookup("dc=example,dc=org");
            result.put("simple_lookup", "SUCCESS");
            result.put("result_type", lookup != null ? lookup.getClass().getSimpleName() : "null");
        } catch (Exception e) {
            result.put("simple_lookup", "FAILED");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/simple-people-test")
    public ResponseEntity<Map<String, String>> simplePeopleTest() {
        Map<String, String> result = new HashMap<>();

        try {
            // En basit test: sadece lookup
            Object lookup = ldapTemplate.lookup("ou=people,dc=example,dc=org");
            result.put("simple_people_lookup", "SUCCESS");
            result.put("result_type", lookup != null ? lookup.getClass().getSimpleName() : "null");
        } catch (Exception e) {
            result.put("simple_people_lookup", "FAILED");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}