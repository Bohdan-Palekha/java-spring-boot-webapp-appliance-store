package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplianceRepository extends JpaRepository<Appliance, Long>, JpaSpecificationExecutor<Appliance> {
    @EntityGraph(attributePaths = {"manufacturer"})
    @Override
    Page<Appliance> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"manufacturer"})
    @Override
    Optional<Appliance> findById(Long id);

    @EntityGraph(attributePaths = {"manufacturer"})
    @Override
    Page<Appliance> findAll(Specification<Appliance> spec, Pageable pageable);

    boolean existsByManufacturerId(Long manufacturerId);

    @Query("SELECT COUNT(a) FROM Appliance a")
    long countAll();

    long countByCategory(Category category);
}
