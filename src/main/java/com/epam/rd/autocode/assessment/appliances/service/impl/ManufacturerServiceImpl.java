package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerHasAppliancesException;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerNameTakenException;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<Manufacturer> getAllManufacturers() {
        return repo.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Manufacturer> getPaginated(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Manufacturer getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ManufacturerNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return repo.existsByName(name);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Manufacturer create(ManufacturerFormDTO dto) {
        if (repo.existsByName(dto.getName())) throw new ManufacturerNameTakenException(dto.getName());
        Manufacturer m = new Manufacturer();
        m.setName(dto.getName().trim());
        Manufacturer saved = repo.save(m);
        log.info("Manufacturer created: id={}, name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Manufacturer update(Long id, ManufacturerFormDTO dto) {
        Manufacturer m = repo.findById(id).orElseThrow(() -> new ManufacturerNotFoundException(id));
        if (repo.existsByNameAndIdNot(dto.getName(), id)) throw new ManufacturerNameTakenException(dto.getName());
        m.setName(dto.getName().trim());
        Manufacturer saved = repo.save(m);
        log.info("Manufacturer updated: id={}, name='{}'", id, saved.getName());
        return saved;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Manufacturer m = repo.findById(id).orElseThrow(() -> new ManufacturerNotFoundException(id));
        if (repo.existsAppliancesForManufacturer(id)) throw new ManufacturerHasAppliancesException(id);
        repo.delete(m);
        log.info("Manufacturer deleted: id={}", id);
    }
}
