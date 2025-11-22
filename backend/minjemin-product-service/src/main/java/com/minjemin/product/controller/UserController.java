package com.minjemin.product.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public String getUserId(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getSubject();
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }
}
