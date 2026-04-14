package com.epam.rd.autocode.assessment.appliances.config;

import com.epam.rd.autocode.assessment.appliances.security.CustomLoginSuccessHandler;
import com.epam.rd.autocode.assessment.appliances.security.CustomUserDetailsService;
import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtAuthFilter;
import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtUtil;
import com.epam.rd.autocode.assessment.appliances.security.oauth2.CustomOAuth2UserService;
import com.epam.rd.autocode.assessment.appliances.security.oauth2.CustomOidcUserService;
import com.epam.rd.autocode.assessment.appliances.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final JwtUtil jwtUtil;
    private final CustomOidcUserService customOidcUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/appliances", "/appliances/**",
                                "/register", "/login", "/logout",
                                "/forgot-password", "/reset-password",
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/access-denied"
                        ).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/h2-console/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/orders/**", "/profile/**").hasRole("CLIENT")
                        .requestMatchers("/dashboard/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        // JWT handler
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/login?error=true")
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // CLear JWT
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.addCookie(jwtUtil.deleteJwtCookie());
                            res.sendRedirect("/login?logout=true");
                        })
                        .permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(info -> info
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
                .headers(h -> h.frameOptions(fo -> fo.sameOrigin()))
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public LoginAttemptFailureHandler loginFailureHandler() {
        return new LoginAttemptFailureHandler();
    }
}