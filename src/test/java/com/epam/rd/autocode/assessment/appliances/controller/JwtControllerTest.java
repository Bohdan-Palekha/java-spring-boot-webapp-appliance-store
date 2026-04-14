package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.controller.api.JwtController;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.RefreshToken;
import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtUtil;
import com.epam.rd.autocode.assessment.appliances.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtController Unit Tests")
class JwtControllerTest {

    @Mock
    private AuthenticationManager authManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RefreshTokenService refreshTokenService;
    @InjectMocks
    private JwtController controller;

    private Client alice;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        alice = new Client(1L, "Alice", "alice@example.com", "pw", null);

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-value");
        refreshToken.setUser(alice);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(604800));
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("returns 200 with accessToken, refreshToken, tokenType on valid credentials")
        @SuppressWarnings("unchecked")
        void login_validCredentials_returnsTokens() {
            Authentication auth = new UsernamePasswordAuthenticationToken("alice@example.com", null,
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
            given(authManager.authenticate(any())).willReturn(auth);
            given(jwtUtil.generateAccessToken("alice@example.com")).willReturn("access-token-value");
            given(refreshTokenService.create("alice@example.com")).willReturn(refreshToken);

            Map<String, String> body = Map.of("email", "alice@example.com", "password", "Password1!");
            ResponseEntity<?> response = controller.login(body);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertThat(responseBody).containsEntry("accessToken", "access-token-value");
            assertThat(responseBody).containsEntry("refreshToken", "refresh-token-value");
            assertThat(responseBody).containsEntry("tokenType", "Bearer");
        }

        @Test
        @DisplayName("propagates BadCredentialsException on invalid credentials")
        void login_invalidCredentials_throwsBadCredentials() {
            given(authManager.authenticate(any())).willThrow(new BadCredentialsException("Bad credentials"));

            Map<String, String> body = Map.of("email", "alice@example.com", "password", "wrongpass");

            assertThatThrownBy(() -> controller.login(body))
                    .isInstanceOf(BadCredentialsException.class);
            then(jwtUtil).should(never()).generateAccessToken(any());
            then(refreshTokenService).should(never()).create(any());
        }

        @Test
        @DisplayName("passes email and password to AuthenticationManager")
        void login_passesCredentialsToAuthManager() {
            Authentication auth = new UsernamePasswordAuthenticationToken("alice@example.com", null);
            given(authManager.authenticate(any())).willReturn(auth);
            given(jwtUtil.generateAccessToken(any())).willReturn("access");
            given(refreshTokenService.create(any())).willReturn(refreshToken);

            controller.login(Map.of("email", "alice@example.com", "password", "Password1!"));

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            then(authManager).should().authenticate(captor.capture());
            assertThat(captor.getValue().getPrincipal()).isEqualTo("alice@example.com");
            assertThat(captor.getValue().getCredentials()).isEqualTo("Password1!");
        }
    }

    @Nested
    @DisplayName("refresh()")
    class RefreshTests {

        @Test
        @DisplayName("returns 200 with new accessToken for valid refresh token")
        @SuppressWarnings("unchecked")
        void refresh_validToken_returnsNewAccessToken() {
            given(refreshTokenService.verify("refresh-token-value")).willReturn(refreshToken);
            given(jwtUtil.generateAccessToken("alice@example.com")).willReturn("new-access-token");

            ResponseEntity<?> response = controller.refresh(Map.of("refreshToken", "refresh-token-value"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertThat(responseBody).containsEntry("accessToken", "new-access-token");
            assertThat(responseBody).containsEntry("tokenType", "Bearer");
        }

        @Test
        @DisplayName("propagates IllegalArgumentException for invalid/expired refresh token")
        void refresh_invalidToken_throwsException() {
            given(refreshTokenService.verify("bad-token")).willThrow(new IllegalArgumentException("invalid"));

            assertThatThrownBy(() -> controller.refresh(Map.of("refreshToken", "bad-token")))
                    .isInstanceOf(IllegalArgumentException.class);
            then(jwtUtil).should(never()).generateAccessToken(any());
        }
    }

    @Nested
    @DisplayName("logout()")
    class LogoutTests {

        @Test
        @DisplayName("returns 200 with success message and revokes refresh token")
        @SuppressWarnings("unchecked")
        void logout_validToken_revokesAndReturns200() {
            ResponseEntity<?> response = controller.logout(Map.of("refreshToken", "refresh-token-value"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertThat(responseBody).containsKey("message");
            then(refreshTokenService).should().revoke("refresh-token-value");
        }

        @Test
        @DisplayName("does not throw when token to revoke is not found")
        void logout_unknownToken_doesNotThrow() {
            // revoke() is a fire-and-forget — service handles missing tokens gracefully
            assertThatCode(() -> controller.logout(Map.of("refreshToken", "no-such-token")))
                    .doesNotThrowAnyException();
        }
    }
}
