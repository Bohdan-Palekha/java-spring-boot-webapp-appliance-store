package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.PasswordResetToken;
import com.epam.rd.autocode.assessment.appliances.repository.PasswordResetTokenRepository;
import com.epam.rd.autocode.assessment.appliances.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService Unit Tests")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private PasswordResetTokenRepository tokenRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private PasswordResetService service;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;
    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;

    private Client alice;

    @BeforeEach
    void setUp() {
        alice = new Client(1L, "Alice", "alice@example.com", "encoded_pw", null);
    }

    @Nested
    @DisplayName("sendResetEmail()")
    class SendResetEmailTests {

        @Test
        @DisplayName("saves token and sends email when user exists")
        void sendResetEmail_knownEmail_savesTokenAndSendsMail() {
            given(userRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(tokenRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.sendResetEmail("alice@example.com", "http://localhost:8080");

            then(tokenRepo).should().save(tokenCaptor.capture());
            PasswordResetToken saved = tokenCaptor.getValue();
            assertThat(saved.getToken()).isNotBlank();
            assertThat(saved.getUser()).isEqualTo(alice);
            assertThat(saved.getExpiresAt()).isAfter(Instant.now());

            then(mailSender).should().send(mailCaptor.capture());
            SimpleMailMessage mail = mailCaptor.getValue();
            assertThat(mail.getTo()).containsExactly("alice@example.com");
            assertThat(mail.getText()).contains("reset-password?token=");
        }

        @Test
        @DisplayName("does nothing silently when email is not registered")
        void sendResetEmail_unknownEmail_doesNothing() {
            given(userRepo.findByEmail("nobody@example.com")).willReturn(Optional.empty());

            assertThatCode(() -> service.sendResetEmail("nobody@example.com", "http://localhost"))
                    .doesNotThrowAnyException();

            then(tokenRepo).should(never()).save(any());
            then(mailSender).should(never()).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("normalizes email to lowercase before lookup")
        void sendResetEmail_uppercaseEmail_normalizesBeforeLookup() {
            given(userRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(tokenRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.sendResetEmail("ALICE@EXAMPLE.COM", "http://localhost:8080");

            then(userRepo).should().findByEmail("alice@example.com");
        }

        @Test
        @DisplayName("does not propagate mail send exceptions to the caller")
        void sendResetEmail_mailFailure_doesNotThrow() {
            given(userRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(tokenRepo.save(any())).willAnswer(inv -> inv.getArgument(0));
            willThrow(new RuntimeException("SMTP error")).given(mailSender).send(any(SimpleMailMessage.class));

            assertThatCode(() -> service.sendResetEmail("alice@example.com", "http://localhost"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("isValidToken()")
    class IsValidTokenTests {

        @Test
        @DisplayName("returns true for an unused, non-expired token")
        void isValidToken_validToken_returnsTrue() {
            PasswordResetToken token = new PasswordResetToken();
            token.setToken("abc-123");
            token.setUsed(false);
            token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
            given(tokenRepo.findByToken("abc-123")).willReturn(Optional.of(token));

            assertThat(service.isValidToken("abc-123")).isTrue();
        }

        @Test
        @DisplayName("returns false for an expired token")
        void isValidToken_expiredToken_returnsFalse() {
            PasswordResetToken token = new PasswordResetToken();
            token.setToken("abc-123");
            token.setUsed(false);
            token.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
            given(tokenRepo.findByToken("abc-123")).willReturn(Optional.of(token));

            assertThat(service.isValidToken("abc-123")).isFalse();
        }

        @Test
        @DisplayName("returns false for a token already used")
        void isValidToken_usedToken_returnsFalse() {
            PasswordResetToken token = new PasswordResetToken();
            token.setToken("abc-123");
            token.setUsed(true);
            token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
            given(tokenRepo.findByToken("abc-123")).willReturn(Optional.of(token));

            assertThat(service.isValidToken("abc-123")).isFalse();
        }

        @Test
        @DisplayName("returns false when token does not exist")
        void isValidToken_notFound_returnsFalse() {
            given(tokenRepo.findByToken("no-such-token")).willReturn(Optional.empty());
            assertThat(service.isValidToken("no-such-token")).isFalse();
        }
    }

    @Nested
    @DisplayName("resetPassword()")
    class ResetPasswordTests {

        @Test
        @DisplayName("updates user password and marks token as used")
        void resetPassword_validToken_updatesPasswordAndMarksUsed() {
            PasswordResetToken token = new PasswordResetToken();
            token.setToken("valid-token");
            token.setUsed(false);
            token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
            token.setUser(alice);

            given(tokenRepo.findByToken("valid-token")).willReturn(Optional.of(token));
            given(passwordEncoder.encode("NewPassword1!")).willReturn("encoded_new");
            given(userRepo.save(any())).willReturn(alice);
            given(tokenRepo.save(any())).willReturn(token);

            service.resetPassword("valid-token", "NewPassword1!");

            assertThat(alice.getPassword()).isEqualTo("encoded_new");
            assertThat(token.isUsed()).isTrue();
            then(userRepo).should().save(alice);
            then(tokenRepo).should().save(token);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for expired or used token")
        void resetPassword_invalidToken_throwsException() {
            given(tokenRepo.findByToken("bad-token")).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.resetPassword("bad-token", "NewPassword1!"))
                    .isInstanceOf(IllegalArgumentException.class);

            then(userRepo).should(never()).save(any());
        }
    }
}
