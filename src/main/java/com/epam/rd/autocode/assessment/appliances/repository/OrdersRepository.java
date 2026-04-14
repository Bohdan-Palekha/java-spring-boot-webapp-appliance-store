package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.client " +
            "JOIN FETCH o.orderRowSet r " +
            "JOIN FETCH r.appliance " +
            "WHERE o.client.id = :cid ORDER BY o.id DESC")
    List<Orders> findByClientIdWithFullDetails(@Param("cid") Long clientId);

    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.client LEFT JOIN FETCH o.employee " +
            "JOIN FETCH o.orderRowSet r JOIN FETCH r.appliance " +
            "WHERE o.id = :id AND o.client.id = :cid")
    Optional<Orders> findByIdAndClientIdWithDetails(@Param("id") Long orderId, @Param("cid") Long clientId);

    // FIX: @EntityGraph + @Query is invalid — use JOIN FETCH in the query itself instead
    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.client LEFT JOIN FETCH o.employee " +
            "ORDER BY o.id DESC")
    Page<Orders> findAllWithDetails(Pageable pageable);

    // FIX: same — replaced @EntityGraph with JOIN FETCH inside the query
    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.client LEFT JOIN FETCH o.employee " +
            "WHERE (:approved IS NULL AND o.approved IS NULL) " +
            "OR (:approved IS NOT NULL AND o.approved = :approved) " +
            "ORDER BY o.id DESC")
    Page<Orders> findByApprovedStatus(@Param("approved") Boolean approved, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.client LEFT JOIN FETCH o.employee " +
            "JOIN FETCH o.orderRowSet r JOIN FETCH r.appliance " +
            "WHERE o.id = :id")
    Optional<Orders> findByIdWithFullDetails(@Param("id") Long orderId);

    // Fetch order for safe deletion
    @Query("SELECT o FROM Orders o WHERE o.employee.id = :empId")
    List<Orders> findByEmployeeId(@Param("empId") Long empId);

    long countByApproved(Boolean approved);
}
