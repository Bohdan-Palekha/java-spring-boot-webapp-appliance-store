package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.PasswordResetToken;
import com.epam.rd.autocode.assessment.appliances.model.User;
import com.epam.rd.autocode.assessment.appliances.repository.PasswordResetTokenRepository;
import com.epam.rd.autocode.assessment.appliances.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public void sendResetEmail(String email, String baseUrl) {
        userRepo.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            String raw = UUID.randomUUID().toString();

            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(raw);
            prt.setUser(user);
            prt.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
            tokenRepo.save(prt);

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setSubject("Password Reset — Appliance Store");
            msg.setText(
                    "Hello " + user.getName() + ",\n\n" +
                            "Click the link below to reset your password (valid for 1 hour):\n" +
                            baseUrl + "/reset-password?token=" + raw + "\n\n" +
                            "If you did not request this, ignore this email."
            );

            try {
                mailSender.send(msg);
                log.info("[PASSWORD-RESET] Reset email sent to {}", user.getEmail());
            } catch (Exception e) {
                log.error("[PASSWORD-RESET] Failed to send email to {}: {}", user.getEmail(), e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public boolean isValidToken(String token) {
        return tokenRepo.findByToken(token)
                .map(t -> !t.isUsed() && t.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepo.findByToken(token)
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        prt.setUsed(true);
        tokenRepo.save(prt);

        log.info("[PASSWORD-RESET] Password successfully reset for: {}", user.getEmail());
    }
}
