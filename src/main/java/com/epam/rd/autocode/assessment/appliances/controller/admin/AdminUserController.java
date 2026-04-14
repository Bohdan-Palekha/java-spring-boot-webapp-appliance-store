package com.epam.rd.autocode.assessment.appliances.controller.admin;

import com.epam.rd.autocode.assessment.appliances.dto.ClientEditDTO;
import com.epam.rd.autocode.assessment.appliances.dto.RegisterFormDTO;
import com.epam.rd.autocode.assessment.appliances.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    //USERS

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        model.addAttribute("users", userService.searchUsers(keyword,
                PageRequest.of(page, size, Sort.by("name"))).getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "admin/users/list";
    }

    //CLIENTS

    @GetMapping("/clients")
    public String clients(@RequestParam(defaultValue = "0") int page, Model model) {
        var p = userService.getAllClients(PageRequest.of(page, 20, Sort.by("name")));
        model.addAttribute("clients", p.getContent());
        model.addAttribute("totalPages", p.getTotalPages());
        model.addAttribute("currentPage", page);
        return "admin/users/clients";
    }

    @GetMapping("/clients/new")
    public String createClientForm(Model model) {
        model.addAttribute("form", new RegisterFormDTO());
        return "admin/users/client-form";
    }

    @PostMapping("/clients")
    public String createClient(@Valid @ModelAttribute("form") RegisterFormDTO dto,
                               BindingResult result, Model model, RedirectAttributes ra) {
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            result.rejectValue("confirmPassword", "error", "Passwords do not match.");
        if (result.hasErrors()) return "admin/users/client-form";
        userService.registerClient(dto);
        ra.addFlashAttribute("successMessage", "client.created.success");
        return "redirect:/admin/users/clients";
    }

    @GetMapping("/clients/{id}/edit")
    public String editClientForm(@PathVariable Long id, Model model) {
        var user = userService.getById(id);
        ClientEditDTO dto = new ClientEditDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        model.addAttribute("form", dto);
        model.addAttribute("clientId", id);
        return "admin/users/client-edit-form";
    }

    @PostMapping("/clients/{id}")
    public String updateClient(@PathVariable Long id,
                               @Valid @ModelAttribute("form") ClientEditDTO dto,
                               BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("clientId", id);
            return "admin/users/client-edit-form";
        }
        userService.updateClient(id, dto);
        ra.addFlashAttribute("successMessage", "client.updated.success");
        return "redirect:/admin/users/clients";
    }

    @PostMapping("/clients/{id}/delete")
    public String deleteClient(@PathVariable Long id, RedirectAttributes ra) {
        userService.deleteUser(id);
        ra.addFlashAttribute("successMessage", "client.deleted.success");
        return "redirect:/admin/users/clients";
    }

    // EMPLOYEE

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public String employees(@RequestParam(defaultValue = "0") int page, Model model) {
        var p = userService.getAllEmployees(PageRequest.of(page, 20, Sort.by("name")));
        model.addAttribute("employees", p.getContent());
        model.addAttribute("totalPages", p.getTotalPages());
        model.addAttribute("currentPage", page);
        return "admin/users/employees";
    }

    @GetMapping("/employees/new")
    public String createEmployeeForm(Model model) {
        model.addAttribute("form", new RegisterFormDTO());
        model.addAttribute("department", "General");
        return "admin/users/employee-form";
    }

    @PostMapping("/employees")
    public String createEmployee(@Valid @ModelAttribute("form") RegisterFormDTO dto,
                                 @RequestParam(defaultValue = "General") String department,
                                 BindingResult result, Model model, RedirectAttributes ra) {
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            result.rejectValue("confirmPassword", "error", "Passwords do not match.");
        if (result.hasErrors()) {
            model.addAttribute("department", department);
            return "admin/users/employee-form";
        }
        userService.createEmployee(dto, department);
        ra.addFlashAttribute("successMessage", "employee.created.success");
        return "redirect:/admin/users/employees";
    }

    @PostMapping("/{id}/department")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public String updateDepartment(@PathVariable Long id,
                                   @RequestParam String department,
                                   RedirectAttributes ra) {
        userService.updateDepartment(id, department);
        ra.addFlashAttribute("successMessage", "employee.department.updated");
        return "redirect:/admin/users/employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes ra) {
        userService.deleteEmployee(id);
        ra.addFlashAttribute("successMessage", "employee.deleted.success");
        return "redirect:/admin/users/employees";
    }
}
