package com.epam.rd.autocode.assessment.appliances.controller.admin;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceFormDTO;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
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
@RequestMapping("/admin/appliances")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
public class AdminApplianceController {
    private final ApplianceService applianceService;
    private final ManufacturerService manufacturerService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(defaultValue = "id") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       Model model) {
        String safe = switch (sortBy) {
            case "name", "price", "category" -> sortBy;
            default -> "id";
        };
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(safe).ascending() : Sort.by(safe).descending();
        var p = applianceService.getAll(PageRequest.of(page, size, sort));
        model.addAttribute("appliances", p.getContent());
        model.addAttribute("totalPages", p.getTotalPages());
        model.addAttribute("totalItems", p.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", safe);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        return "admin/appliances/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        populateModel(model, new ApplianceFormDTO(), false, "/admin/appliances");
        return "admin/appliances/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ApplianceFormDTO dto, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateModel(model, dto, false, "/admin/appliances");
            return "admin/appliances/form";
        }
        applianceService.create(dto);
        ra.addFlashAttribute("successMessage", "appliance.created.success");
        return "redirect:/admin/appliances";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Appliance a = applianceService.getById(id);
        ApplianceFormDTO dto = toForm(a);
        populateModel(model, dto, true, "/admin/appliances/" + id);
        model.addAttribute("applianceId", id);
        return "admin/appliances/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") ApplianceFormDTO dto, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateModel(model, dto, true, "/admin/appliances/" + id);
            model.addAttribute("applianceId", id);
            return "admin/appliances/form";
        }
        applianceService.update(id, dto);
        ra.addFlashAttribute("successMessage", "appliance.updated.success");
        return "redirect:/admin/appliances";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        applianceService.delete(id);
        ra.addFlashAttribute("successMessage", "appliance.deleted.success");
        return "redirect:/admin/appliances";
    }

    private void populateModel(Model m, ApplianceFormDTO dto, boolean isEdit, String action) {
        m.addAttribute("form", dto);
        m.addAttribute("manufacturers", manufacturerService.getAllManufacturers());
        m.addAttribute("categories", Category.values());
        m.addAttribute("powerTypes", PowerType.values());
        m.addAttribute("isEdit", isEdit);
        m.addAttribute("formAction", action);
    }

    private ApplianceFormDTO toForm(Appliance a) {
        ApplianceFormDTO f = new ApplianceFormDTO();
        f.setName(a.getName());
        f.setCategory(a.getCategory());
        f.setModel(a.getModel());
        f.setManufacturerId(a.getManufacturer().getId());
        f.setPowerType(a.getPowerType());
        f.setCharacteristic(a.getCharacteristic());
        f.setDescription(a.getDescription());
        f.setPower(a.getPower());
        f.setPrice(a.getPrice());
        return f;
    }
}
