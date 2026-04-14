package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService resetService;

    @GetMapping("/forgot-password")
    public String forgotForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotSubmit(@RequestParam String email,
                               HttpServletRequest request,
                               RedirectAttributes ra) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                (isStandardPort(request) ? "" : ":" + request.getServerPort());
        resetService.sendResetEmail(email, baseUrl);
        ra.addFlashAttribute("successMessage", "password.reset.sent");
        return "redirect:/login";
    }


    @GetMapping("/reset-password")
    public String resetForm(@RequestParam String token, Model model) {
        if (!resetService.isValidToken(token)) {
            return "redirect:/login?tokenExpired=true";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(@RequestParam String token,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              RedirectAttributes ra) {
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMessage", "user.password.mismatch");
            return "redirect:/reset-password?token=" + token;
        }
        try {
            resetService.resetPassword(token, password);
            ra.addFlashAttribute("successMessage", "password.reset.success");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "password.reset.invalid");
        }
        return "redirect:/login";
    }

    private boolean isStandardPort(HttpServletRequest req) {
        int port = req.getServerPort();
        return ("http".equals(req.getScheme()) && port == 80)
                || ("https".equals(req.getScheme()) && port == 443);
    }
}
