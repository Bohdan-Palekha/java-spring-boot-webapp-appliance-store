package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    Optional<Manufacturer> findByName(String name);

    List<Manufacturer> findAllByOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long excludeId);

    @Query("SELECT CASE WHEN COUNT(a)>0 THEN true ELSE false END FROM Appliance a WHERE a.manufacturer.id=:id")
    boolean existsAppliancesForManufacturer(@Param("id") Long manufacturerId);
}
