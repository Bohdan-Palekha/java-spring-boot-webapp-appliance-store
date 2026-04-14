package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.RefreshToken;
import com.epam.rd.autocode.assessment.appliances.repository.RefreshTokenRepository;
import com.epam.rd.autocode.assessment.appliances.repository.UserRepository;
import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtUtil;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private RefreshTokenService service;

    @Captor
    private ArgumentCaptor<RefreshToken> captor;

    private Client alice;

    @BeforeEach
    void setUp() {
        alice = new Client(1L, "Alice", "alice@example.com", "encoded_pw", null);
        ReflectionTestUtils.setField(service, "refreshMs", 604800000L);
    }

    @Test
    @DisplayName("create() deletes old tokens, saves new token with expiry")
    void create_deletesOldAndSavesNew() {
        given(userRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
        given(jwtUtil.generateRefreshToken("alice@example.com")).willReturn("refresh-token-value");
        given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

        RefreshToken result = service.create("alice@example.com");

        then(repo).should().deleteAllByUserId(1L);
        then(repo).should().save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getToken()).isEqualTo("refresh-token-value");
        assertThat(saved.getUser()).isEqualTo(alice);
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
    }

    @Nested
    @DisplayName("verify()")
    class VerifyTests {

        @Test
        @DisplayName("returns token when it is valid and not revoked")
        void verify_validToken_returnsToken() {
            RefreshToken token = new RefreshToken();
            token.setToken("valid-refresh");
            token.setRevoked(false);
            token.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
            token.setUser(alice);

            given(repo.findByToken("valid-refresh")).willReturn(Optional.of(token));

            assertThat(service.verify("valid-refresh")).isEqualTo(token);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for expired token")
        void verify_expiredToken_throwsException() {
            RefreshToken token = new RefreshToken();
            token.setToken("expired-refresh");
            token.setRevoked(false);
            token.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

            given(repo.findByToken("expired-refresh")).willReturn(Optional.of(token));

            assertThatThrownBy(() -> service.verify("expired-refresh"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for revoked token")
        void verify_revokedToken_throwsException() {
            RefreshToken token = new RefreshToken();
            token.setToken("revoked-refresh");
            token.setRevoked(true);
            token.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));

            given(repo.findByToken("revoked-refresh")).willReturn(Optional.of(token));

            assertThatThrownBy(() -> service.verify("revoked-refresh"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when token not found")
        void verify_notFound_throwsException() {
            given(repo.findByToken("no-such-token")).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.verify("no-such-token"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("revoke()")
    class RevokeTests {

        @Test
        @DisplayName("marks token as revoked and saves")
        void revoke_existingToken_marksRevoked() {
            RefreshToken token = new RefreshToken();
            token.setToken("token-to-revoke");
            token.setRevoked(false);

            given(repo.findByToken("token-to-revoke")).willReturn(Optional.of(token));
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.revoke("token-to-revoke");

            then(repo).should().save(captor.capture());
            assertThat(captor.getValue().isRevoked()).isTrue();
        }

        @Test
        @DisplayName("does nothing silently when token is not found")
        void revoke_notFound_doesNothing() {
            given(repo.findByToken("missing")).willReturn(Optional.empty());

            assertThatCode(() -> service.revoke("missing")).doesNotThrowAnyException();
            then(repo).should(never()).save(any());
        }
    }
}
