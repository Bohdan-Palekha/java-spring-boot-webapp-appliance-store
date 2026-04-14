package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.RefreshToken;
import com.epam.rd.autocode.assessment.appliances.model.User;
import com.epam.rd.autocode.assessment.appliances.repository.RefreshTokenRepository;
import com.epam.rd.autocode.assessment.appliances.repository.UserRepository;
import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshMs;

    public RefreshToken create(String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        repo.deleteAllByUserId(user.getId()); //1 token per user
        RefreshToken rt = new RefreshToken();
        rt.setToken(jwtUtil.generateRefreshToken(email));
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plusMillis(refreshMs));
        return repo.save(rt);
    }

    @Transactional(readOnly = true)
    public RefreshToken verify(String token) {
        return repo.findByToken(token)
                .filter(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));
    }

    public void revoke(String token) {
        repo.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            repo.save(rt);
        });
    }
}
