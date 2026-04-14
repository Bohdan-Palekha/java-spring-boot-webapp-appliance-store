package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.OrdersService;
import com.epam.rd.autocode.assessment.appliances.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ApplianceService applianceService;
    private final OrdersService ordersService;
    private final UserService userService;

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        model.addAttribute("featured", applianceService.getFeatured(8));
        model.addAttribute("categories", Category.values());
        model.addAttribute("authenticated", isLoggedIn(auth));
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        boolean isAdmin = hasRole(auth, "ROLE_ADMIN"), isEmployee = hasRole(auth, "ROLE_EMPLOYEE");
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isEmployee", isEmployee);
        model.addAttribute("isClient", hasRole(auth, "ROLE_CLIENT"));
        if (isAdmin || isEmployee) {
            model.addAttribute("pendingOrders", ordersService.countPending());
            model.addAttribute("approvedOrders", ordersService.countApproved());
            model.addAttribute("rejectedOrders", ordersService.countRejected());
            model.addAttribute("totalAppliances", applianceService.countAll());
        }
        if (isAdmin) {
            model.addAttribute("totalClients", userService.countClients());
            model.addAttribute("totalEmployees", userService.countEmployees());
        }
        if (hasRole(auth, "ROLE_CLIENT")) {
            model.addAttribute("myOrders", ordersService.getOrdersForClient(auth.getName()));
        }
        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", "You do not have permission to access this page.");
        return "error/403";
    }

    private boolean isLoggedIn(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
