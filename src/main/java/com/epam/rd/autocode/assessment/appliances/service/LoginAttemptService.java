package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.LoginAttempt;
import com.epam.rd.autocode.assessment.appliances.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final LoginAttemptRepository repo;

    public void recordFailure(String email) {
        LoginAttempt attempt = repo.findByEmail(email).orElseGet(() -> {
            LoginAttempt a = new LoginAttempt();
            a.setEmail(email);
            return a;
        });

        if (attempt.getLockedUntil() != null && attempt.getLockedUntil().isBefore(Instant.now())) {
            attempt.setFailCount(0);
            attempt.setLockedUntil(null);
        }

        attempt.setFailCount(attempt.getFailCount() + 1);
        attempt.setLastFail(Instant.now());

        if (attempt.getFailCount() >= MAX_ATTEMPTS) {
            attempt.setLockedUntil(Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES));
            log.warn("[SECURITY] Account locked after {} failed attempts: {}", MAX_ATTEMPTS, email);
        }

        repo.save(attempt);
    }

    public void recordSuccess(String email) {
        repo.findByEmail(email).ifPresent(a -> {
            a.setFailCount(0);
            a.setLockedUntil(null);
            repo.save(a);
        });
    }

    @Transactional(readOnly = true)
    public boolean isLocked(String email) {
        return repo.findByEmail(email)
                .map(a -> a.getLockedUntil() != null && a.getLockedUntil().isAfter(Instant.now()))
                .orElse(false);
    }
}
