package com.epam.rd.autocode.assessment.appliances.controller.admin;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerFormDTO;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
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
@RequestMapping("/admin/manufacturers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
public class AdminManufacturerController {
    private final ManufacturerService manufacturerService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Model model) {
        var p = manufacturerService.getPaginated(PageRequest.of(page, size, Sort.by("name")));
        model.addAttribute("manufacturers", p.getContent());
        model.addAttribute("totalPages", p.getTotalPages());
        model.addAttribute("currentPage", page);
        return "admin/manufacturers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ManufacturerFormDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/admin/manufacturers");
        return "admin/manufacturers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ManufacturerFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/admin/manufacturers");
            return "admin/manufacturers/form";
        }
        manufacturerService.create(dto);
        ra.addFlashAttribute("successMessage", "manufacturer.created.success");
        return "redirect:/admin/manufacturers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var m = manufacturerService.getById(id);
        var dto = new ManufacturerFormDTO();
        dto.setName(m.getName());
        model.addAttribute("form", dto);
        model.addAttribute("mfrId", id);
        model.addAttribute("isEdit", true);
        model.addAttribute("formAction", "/admin/manufacturers/" + id);
        return "admin/manufacturers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ManufacturerFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("mfrId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/admin/manufacturers/" + id);
            return "admin/manufacturers/form";
        }
        manufacturerService.update(id, dto);
        ra.addFlashAttribute("successMessage", "manufacturer.updated.success");
        return "redirect:/admin/manufacturers";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        manufacturerService.delete(id);
        ra.addFlashAttribute("successMessage", "manufacturer.deleted.success");
        return "redirect:/admin/manufacturers";
    }
}
