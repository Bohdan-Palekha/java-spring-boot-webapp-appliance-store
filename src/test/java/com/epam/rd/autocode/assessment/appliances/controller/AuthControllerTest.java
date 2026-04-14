package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.RegisterFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.DuplicateEmailException;
import com.epam.rd.autocode.assessment.appliances.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authManager;
    @InjectMocks
    private AuthController controller;

    private Model model;
    private RegisterFormDTO validDto;
    private BindingResult noErrors;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();

        validDto = new RegisterFormDTO();
        validDto.setName("Alice");
        validDto.setEmail("alice@example.com");
        validDto.setPassword("Password1!");
        validDto.setConfirmPassword("Password1!");

        noErrors = new BeanPropertyBindingResult(validDto, "form");
    }

    @Nested
    @DisplayName("loginPage()")
    class LoginPageTests {

        @Test
        @DisplayName("returns 'auth/login' when user is not authenticated")
        void loginPage_notAuthenticated_returnsLoginView() {
            Authentication auth = mock(Authentication.class);
            given(auth.isAuthenticated()).willReturn(false); // Simulate NOT logged in

            String view = controller.loginPage(auth);

            assertThat(view).isEqualTo("auth/login");
        }

        @Test
        @DisplayName("loginPage() - authenticated - redirects to dashboard")
        void loginPage_authenticated_redirectsToDashboard() {
            Authentication auth = mock(Authentication.class);
            given(auth.isAuthenticated()).willReturn(true);
            String view = controller.loginPage(auth);

            assertThat(view).isEqualTo("redirect:/dashboard");
        }


        @Nested
        @DisplayName("registerForm()")
        class RegisterFormTests {

            @Test
            @DisplayName("returns 'auth/register' and adds empty DTO to model")
            void registerForm_notAuthenticated_returnsRegisterView() {
                String view = controller.registerForm(model, null);

                assertThat(view).isEqualTo("auth/register");
                assertThat(model.asMap()).containsKey("form");
            }

            @Test
            @DisplayName("registerForm() - authenticated - redirects to dashboard")
            void registerForm_authenticated_redirectsToDashboard() {
                Authentication auth = mock(Authentication.class);
                given(auth.isAuthenticated()).willReturn(true);
                Model model = new ExtendedModelMap();
                String view = controller.registerForm(model, auth);
                assertThat(view).isEqualTo("redirect:/dashboard");
            }

            @Nested
            @DisplayName("processRegister()")
            class ProcessRegisterTests {

                @Mock
                private HttpServletRequest request;
                @Mock
                private jakarta.servlet.http.HttpServletResponse response;
                @Mock
                private com.epam.rd.autocode.assessment.appliances.security.jwt.JwtUtil jwtUtil;

                @Test
                @DisplayName("registers successfully and redirects to /profile")
                void processRegister_validData_redirectsToProfile() {
                    given(jwtUtil.generateAccessToken(anyString())).willReturn("mock-token");
                    given(jwtUtil.createJwtCookie(anyString())).willReturn(new jakarta.servlet.http.Cookie("JWT_TOKEN", "mock-token"));

                    var ra = new RedirectAttributesModelMap();

                    String view = controller.processRegister(validDto, noErrors, request, ra, response, jwtUtil);

                    assertThat(view).isEqualTo("redirect:/profile");
                    then(userService).should().registerClient(validDto);
                    then(response).should().addCookie(any(jakarta.servlet.http.Cookie.class));
                    assertThat(ra.getFlashAttributes()).containsKey("successMessage");
                }

                @Test
                @DisplayName("returns register form when passwords don't match")
                void processRegister_passwordMismatch_returnsRegisterForm() {
                    validDto.setConfirmPassword("DifferentPassword!");
                    BindingResult result = new BeanPropertyBindingResult(validDto, "form");

                    String view = controller.processRegister(validDto, result, request,
                            new RedirectAttributesModelMap(), response, jwtUtil);

                    assertThat(view).isEqualTo("auth/register");
                    assertThat(result.hasErrors()).isTrue();
                    then(userService).should(never()).registerClient(any());
                }

                @Test
                @DisplayName("returns register form when BindingResult has errors")
                void processRegister_bindingErrors_returnsRegisterForm() {
                    BindingResult result = new BeanPropertyBindingResult(validDto, "form");
                    result.rejectValue("name", "error.name", "Name is required");

                    String view = controller.processRegister(validDto, result, request,
                            new RedirectAttributesModelMap(), response, jwtUtil);

                    assertThat(view).isEqualTo("auth/register");
                    then(userService).should(never()).registerClient(any());
                }

                @Test
                @DisplayName("redirects to /register with error when registration throws")
                void processRegister_serviceThrows_redirectsToRegister() {
                    willThrow(new DuplicateEmailException("alice@example.com")).given(userService).registerClient(any());

                    var ra = new RedirectAttributesModelMap();
                    String view = controller.processRegister(validDto, noErrors, request, ra, response, jwtUtil);

                    assertThat(view).isEqualTo("redirect:/register");
                    assertThat(ra.getFlashAttributes()).containsKey("errorMessage");
                }
            }
        }
    }
}
