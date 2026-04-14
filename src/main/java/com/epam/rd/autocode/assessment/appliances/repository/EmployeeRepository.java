package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    @Override
    Page<Employee> findAll(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE UPPER(e.department)='ADMIN'")
    List<Employee> findAdminEmployees();

    @Query("SELECT COUNT(e) FROM Employee e WHERE UPPER(e.department)='ADMIN'")
    long countAdminEmployees();

    @Query("SELECT CASE WHEN COUNT(e)=1 THEN true ELSE false END FROM Employee e WHERE UPPER(e.department)='ADMIN' AND e.id=:id")
    boolean isLastAdmin(@Param("id") Long targetId);
}
