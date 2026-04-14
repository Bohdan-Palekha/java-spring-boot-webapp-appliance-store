package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetController Unit Tests")
class PasswordResetControllerTest {

    @Mock
    private PasswordResetService resetService;
    @InjectMocks
    private PasswordResetController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    @Test
    @DisplayName("forgotForm() returns 'auth/forgot-password' view")
    void forgotForm_returnsCorrectView() {
        String view = controller.forgotForm();
        assertThat(view).isEqualTo("auth/forgot-password");
    }

    @Nested
    @DisplayName("forgotSubmit()")
    class ForgotSubmitTests {

        @Mock
        private HttpServletRequest request;

        @BeforeEach
        void setupRequest() {
            given(request.getScheme()).willReturn("http");
            given(request.getServerName()).willReturn("localhost");
            given(request.getServerPort()).willReturn(8080);
        }

        @Test
        @DisplayName("calls sendResetEmail and redirects to /login with flash message")
        void forgotSubmit_validEmail_redirectsToLogin() {
            var ra = new RedirectAttributesModelMap();

            String view = controller.forgotSubmit("alice@example.com", request, ra);

            assertThat(view).isEqualTo("redirect:/login");
            then(resetService).should().sendResetEmail(eq("alice@example.com"), contains("localhost"));
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("always redirects (does not reveal whether email exists)")
        void forgotSubmit_unknownEmail_stillRedirectsToLogin() {
            var ra = new RedirectAttributesModelMap();

            String view = controller.forgotSubmit("nobody@example.com", request, ra);

            assertThat(view).isEqualTo("redirect:/login");
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }
    }

    @Nested
    @DisplayName("resetForm()")
    class ResetFormTests {

        @Test
        @DisplayName("returns 'auth/reset-password' and adds token to model for valid token")
        void resetForm_validToken_returnsResetView() {
            given(resetService.isValidToken("valid-token")).willReturn(true);

            String view = controller.resetForm("valid-token", model);

            assertThat(view).isEqualTo("auth/reset-password");
            assertThat(model.asMap().get("token")).isEqualTo("valid-token");
        }

        @Test
        @DisplayName("redirects to /login?tokenExpired=true for invalid token")
        void resetForm_invalidToken_redirectsToLogin() {
            given(resetService.isValidToken("expired-token")).willReturn(false);

            String view = controller.resetForm("expired-token", model);

            assertThat(view).isEqualTo("redirect:/login?tokenExpired=true");
        }
    }

    @Nested
    @DisplayName("resetSubmit()")
    class ResetSubmitTests {

        @Test
        @DisplayName("resets password and redirects to /login with success message")
        void resetSubmit_validToken_resetsAndRedirects() {
            var ra = new RedirectAttributesModelMap();

            String view = controller.resetSubmit("valid-token", "NewPassword1!", "NewPassword1!", ra);

            assertThat(view).isEqualTo("redirect:/login");
            then(resetService).should().resetPassword("valid-token", "NewPassword1!");
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("redirects back to reset form when passwords don't match")
        void resetSubmit_passwordMismatch_redirectsToResetForm() {
            var ra = new RedirectAttributesModelMap();

            String view = controller.resetSubmit("valid-token", "Password1!", "DifferentPass!", ra);

            assertThat(view).startsWith("redirect:/reset-password?token=valid-token");
            assertThat(ra.getFlashAttributes()).containsKey("errorMessage");
            then(resetService).should(never()).resetPassword(any(), any());
        }

        @Test
        @DisplayName("redirects to /login with error when service throws")
        void resetSubmit_serviceThrows_redirectsWithError() {
            willThrow(new IllegalArgumentException("invalid token"))
                    .given(resetService).resetPassword(eq("bad-token"), any());
            var ra = new RedirectAttributesModelMap();

            String view = controller.resetSubmit("bad-token", "NewPassword1!", "NewPassword1!", ra);

            assertThat(view).isEqualTo("redirect:/login");
            assertThat(ra.getFlashAttributes()).containsKey("errorMessage");
        }
    }
}
