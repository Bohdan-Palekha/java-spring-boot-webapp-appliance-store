package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE :kw IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:kw,'%')) ORDER BY u.name")
    Page<User> searchUsers(@Param("kw") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u)=Client")
    long countClients();

    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u)=Employee")
    long countEmployees();
}
