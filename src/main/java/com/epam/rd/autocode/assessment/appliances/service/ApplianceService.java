package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceFormDTO;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ApplianceService {
    Page<Appliance> search(String keyword, Category category, PowerType powerType,
                           BigDecimal minPrice, BigDecimal maxPrice,
                           Long manufacturerId, Pageable pageable);

    Page<Appliance> getAll(Pageable pageable);

    List<Appliance> getFeatured(int limit);

    Appliance getById(Long id);

    Appliance create(ApplianceFormDTO dto);

    Appliance update(Long id, ApplianceFormDTO dto);

    void delete(Long id);

    long countAll();

    long countByCategory(Category category);
}
