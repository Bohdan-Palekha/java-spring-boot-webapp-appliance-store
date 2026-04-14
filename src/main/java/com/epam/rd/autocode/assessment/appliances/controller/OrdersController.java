package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.OrderFormDTO;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.OrdersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class OrdersController {

    private final OrdersService ordersService;
    private final ApplianceService applianceService;

    @GetMapping
    public String myOrders(Authentication auth, Model model) {
        model.addAttribute("orders", ordersService.getOrdersForClient(auth.getName()));
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("order", ordersService.getOrderForClient(id, auth.getName()));
        return "orders/detail";
    }

    @GetMapping("/place")
    public String showPlaceForm(@RequestParam(required = false) Long applianceId, Model model) {
        OrderFormDTO form = new OrderFormDTO();
        if (applianceId != null) {
            form.setApplianceId(applianceId);
            try {
                model.addAttribute("selectedAppliance", applianceService.getById(applianceId));
            } catch (Exception ignored) {
            }
        }
        model.addAttribute("orderForm", form);
        return "orders/create";
    }

    @PostMapping("/place")
    public String placeOrder(@Valid @ModelAttribute("orderForm") OrderFormDTO dto,
                             BindingResult result,
                             Authentication auth,
                             RedirectAttributes ra,
                             Model model) {
        if (result.hasErrors()) {
            if (dto.getApplianceId() != null) {
                try {
                    model.addAttribute("selectedAppliance", applianceService.getById(dto.getApplianceId()));
                } catch (Exception ignored) {
                }
            }
            return "orders/create";
        }
        ordersService.placeOrder(auth.getName(), dto);
        ra.addFlashAttribute("successMessage", "order.placed.success");
        return "redirect:/orders";
    }
}
