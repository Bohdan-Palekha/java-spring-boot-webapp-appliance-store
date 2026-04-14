package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ProfileController {

    private final ClientRepository clientRepository;

    @GetMapping
    public String profile(Authentication auth, Model model) {
        Client client = clientRepository.findByEmail(auth.getName())
                .orElseThrow();
        model.addAttribute("client", client);
        return "profile/view";
    }

    @PostMapping("/card")
    public String updateCard(@RequestParam String card,
                             Authentication auth,
                             RedirectAttributes ra) {
        Client client = clientRepository.findByEmail(auth.getName())
                .orElseThrow();
        String trimmed = (card != null) ? card.trim() : "";
        client.setCard(trimmed.isEmpty() ? null : trimmed);
        clientRepository.save(client);
        ra.addFlashAttribute("successMessage", "profile.card.updated");
        return "redirect:/profile";
    }
}
