package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ClientEditDTO;
import com.epam.rd.autocode.assessment.appliances.dto.RegisterFormDTO;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Client registerClient(RegisterFormDTO dto);

    Client processOAuth2PostLogin(String email, String name); // Added OAuth2 signature

    User getById(Long id);

    User getByEmail(String email);

    Page<User> searchUsers(String keyword, Pageable pageable);

    Page<Client> getAllClients(Pageable pageable);

    Page<Employee> getAllEmployees(Pageable pageable);

    Employee createEmployee(RegisterFormDTO dto, String department);

    void updateDepartment(Long employeeId, String department);

    Client updateClient(Long id, ClientEditDTO dto);

    void deleteUser(Long id);

    void deleteEmployee(Long id);

    boolean existsByEmail(String email);

    long countClients();

    long countEmployees();
}