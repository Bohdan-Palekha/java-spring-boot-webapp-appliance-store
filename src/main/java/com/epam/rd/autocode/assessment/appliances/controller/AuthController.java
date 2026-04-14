package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.RegisterFormDTO;
import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtUtil;
import com.epam.rd.autocode.assessment.appliances.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;

    @GetMapping("/login")
    public String loginPage(Authentication auth) {
        if (isAuthenticated(auth)) return "redirect:/dashboard";
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model, Authentication auth) {
        if (isAuthenticated(auth)) return "redirect:/dashboard";
        model.addAttribute("form", new RegisterFormDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("form") RegisterFormDTO dto,
                                  BindingResult result,
                                  HttpServletRequest request,
                                  RedirectAttributes ra, HttpServletResponse response, JwtUtil jwtUtil) {
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            result.rejectValue("confirmPassword", "error", "Passwords do not match.");
        if (result.hasErrors()) return "auth/register";

        try {
            userService.registerClient(dto);

            String token = jwtUtil.generateAccessToken(dto.getEmail().trim().toLowerCase());
            response.addCookie(jwtUtil.createJwtCookie(token));

            ra.addFlashAttribute("successMessage", "registration.success");
            return "redirect:/profile";
        } catch (Exception ex) {
            log.error("Registration error: {}", ex.getMessage());
            ra.addFlashAttribute("errorMessage", "Registration failed. Please try again.");
            return "redirect:/register";
        }
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}