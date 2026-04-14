package com.epam.rd.autocode.assessment.appliances.security.oauth2;

import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtUtil;
import com.epam.rd.autocode.assessment.appliances.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        String email = principal.getAttribute("email");
        String googleName = principal.getAttribute("name");

        if (email == null) {
            log.warn("[OAUTH2] No email in principal — redirecting to login error");
            getRedirectStrategy().sendRedirect(request, response, "/login?error=oauth");
            return;
        }

        userService.processOAuth2PostLogin(email, googleName);

        String token = jwtUtil.generateAccessToken(email.toLowerCase().trim());
        response.addCookie(jwtUtil.createJwtCookie(token));

        getRedirectStrategy().sendRedirect(request, response, "/dashboard");
    }
}