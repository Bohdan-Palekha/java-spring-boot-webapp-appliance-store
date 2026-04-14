package com.epam.rd.autocode.assessment.appliances.controller.admin;

import com.epam.rd.autocode.assessment.appliances.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
public class AdminOrdersController {

    private final OrdersService ordersService;

    @GetMapping
    public String list(@RequestParam(required = false) Boolean approved,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        var p = (approved != null)
                ? ordersService.getByApprovalStatus(approved, pageable)
                : ordersService.getAllOrders(pageable);

        model.addAttribute("orders", p.getContent());
        model.addAttribute("totalPages", p.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("approved", approved);
        model.addAttribute("pendingCount", ordersService.countPending());
        model.addAttribute("approvedCount", ordersService.countApproved());
        model.addAttribute("rejectedCount", ordersService.countRejected());
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", ordersService.getOrderById(id));
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        ordersService.setApprovalStatus(id, true, auth.getName());
        ra.addFlashAttribute("successMessage", "order.approved.success");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        ordersService.setApprovalStatus(id, false, auth.getName());
        ra.addFlashAttribute("successMessage", "order.rejected.success");
        return "redirect:/admin/orders/" + id;
    }
}
