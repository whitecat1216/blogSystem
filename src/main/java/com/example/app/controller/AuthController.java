package com.example.app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/status")
    public Map<String, Object> status(Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        boolean authenticated = auth != null && auth.isAuthenticated();
        resp.put("authenticated", authenticated);
        if (authenticated) {
            resp.put("username", auth.getName());
            var roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            resp.put("roles", roles);
            resp.put("isAdmin", roles.stream().anyMatch(r -> r.contains("ADMIN")));
        } else {
            resp.put("username", "anonymous");
            resp.put("roles", new String[]{});
            resp.put("isAdmin", false);
        }
        return resp;
    }
}