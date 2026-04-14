package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ApplianceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.specification.ApplianceSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ApplianceServiceImpl implements ApplianceService {

    private final ApplianceRepository applianceRepo;
    private final ManufacturerRepository manufacturerRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<Appliance> search(String keyword, Category category, PowerType powerType,
                                  BigDecimal minPrice, BigDecimal maxPrice,
                                  Long manufacturerId, Pageable pageable) {
        Specification<Appliance> spec = Specification
                .where(ApplianceSpecification.hasKeyword(keyword))
                .and(ApplianceSpecification.hasCategory(category))
                .and(ApplianceSpecification.hasPowerType(powerType))
                .and(ApplianceSpecification.priceBetween(minPrice, maxPrice))
                .and(ApplianceSpecification.hasManufacturer(manufacturerId));
        return applianceRepo.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appliance> getAll(Pageable pageable) {
        return applianceRepo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appliance> getFeatured(int limit) {
        int safe = Math.min(Math.max(limit, 1), 20);
        return applianceRepo.findAll(PageRequest.of(0, safe, Sort.by("id").descending())).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Appliance getById(Long id) {
        return applianceRepo.findById(id).orElseThrow(() -> new ApplianceNotFoundException(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Appliance create(ApplianceFormDTO dto) {
        Manufacturer mfr = manufacturerRepo.findById(dto.getManufacturerId())
                .orElseThrow(() -> new ManufacturerNotFoundException(dto.getManufacturerId()));
        Appliance a = buildAppliance(new Appliance(), dto, mfr);
        Appliance saved = applianceRepo.save(a);
        log.info("Appliance created: id={}, name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Appliance update(Long id, ApplianceFormDTO dto) {
        Appliance existing = applianceRepo.findById(id).orElseThrow(() -> new ApplianceNotFoundException(id));
        Manufacturer mfr = manufacturerRepo.findById(dto.getManufacturerId())
                .orElseThrow(() -> new ManufacturerNotFoundException(dto.getManufacturerId()));
        Appliance saved = applianceRepo.save(buildAppliance(existing, dto, mfr));
        log.info("Appliance updated: id={}", id);
        return saved;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        applianceRepo.findById(id).orElseThrow(() -> new ApplianceNotFoundException(id));
        applianceRepo.deleteById(id);
        log.info("Appliance deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return applianceRepo.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategory(Category category) {
        return applianceRepo.countByCategory(category);
    }

    private Appliance buildAppliance(Appliance a, ApplianceFormDTO dto, Manufacturer mfr) {
        a.setName(dto.getName().trim());
        a.setCategory(dto.getCategory());
        a.setModel(dto.getModel());
        a.setManufacturer(mfr);
        a.setPowerType(dto.getPowerType());
        a.setCharacteristic(dto.getCharacteristic());
        a.setDescription(dto.getDescription());
        a.setPower(dto.getPower());
        a.setPrice(dto.getPrice());
        return a;
    }
}
