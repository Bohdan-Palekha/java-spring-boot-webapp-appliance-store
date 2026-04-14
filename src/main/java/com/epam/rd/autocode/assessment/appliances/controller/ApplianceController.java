package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/appliances")
@RequiredArgsConstructor
public class ApplianceController {

    private final ApplianceService applianceService;
    private final ManufacturerService manufacturerService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Category category,
                       @RequestParam(required = false) PowerType powerType,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) Long manufacturerId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       @RequestParam(defaultValue = "name") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       Model model) {

        String safeSortBy = switch (sortBy) {
            case "price", "name", "power" -> sortBy;
            default -> "name";
        };
        Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(safeSortBy).descending() : Sort.by(safeSortBy).ascending();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 50), sort);

        var pageResult = applianceService.search(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                category, powerType, minPrice, maxPrice, manufacturerId, pageable);

        model.addAttribute("appliances", pageResult.getContent());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPowerType", powerType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedMfr", manufacturerId);
        model.addAttribute("categories", Category.values());
        model.addAttribute("powerTypes", PowerType.values());
        model.addAttribute("manufacturers", manufacturerService.getAllManufacturers());
        model.addAttribute("sortBy", safeSortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        boolean hasFilter = keyword != null || category != null || powerType != null
                || minPrice != null || maxPrice != null || manufacturerId != null;
        model.addAttribute("hasFilters", hasFilter);
        return "appliances/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("appliance", applianceService.getById(id));
        return "appliances/detail";
    }
}
