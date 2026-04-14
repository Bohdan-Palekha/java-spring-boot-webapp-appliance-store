package com.epam.rd.autocode.assessment.appliances.controller.api;

import com.epam.rd.autocode.assessment.appliances.model.RefreshToken;
import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtUtil;
import com.epam.rd.autocode.assessment.appliances.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JwtController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        body.get("email"), body.get("password")));

        String email = auth.getName();
        String accessToken = jwtUtil.generateAccessToken(email);
        RefreshToken rt = refreshTokenService.create(email);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", rt.getToken(),
                "tokenType", "Bearer"
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        RefreshToken rt = refreshTokenService.verify(body.get("refreshToken"));
        String newAccess = jwtUtil.generateAccessToken(rt.getUser().getEmail());
        return ResponseEntity.ok(Map.of("accessToken", newAccess, "tokenType", "Bearer"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        refreshTokenService.revoke(body.get("refreshToken"));
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
