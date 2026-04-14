package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerFormDTO;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ManufacturerService {
    List<Manufacturer> getAllManufacturers();

    Page<Manufacturer> getPaginated(Pageable pageable);

    Manufacturer getById(Long id);

    Manufacturer create(ManufacturerFormDTO dto);

    Manufacturer update(Long id, ManufacturerFormDTO dto);

    void delete(Long id);

    boolean existsByName(String name);
}
