package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.ClientEditDTO;
import com.epam.rd.autocode.assessment.appliances.dto.RegisterFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.DuplicateEmailException;
import com.epam.rd.autocode.assessment.appliances.exception.LastAdminException;
import com.epam.rd.autocode.assessment.appliances.exception.UserNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.model.Orders;
import com.epam.rd.autocode.assessment.appliances.model.User;
import com.epam.rd.autocode.assessment.appliances.repository.*;
import com.epam.rd.autocode.assessment.appliances.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final ClientRepository clientRepo;
    private final EmployeeRepository employeeRepo;
    private final PasswordEncoder passwordEncoder;

    // for handling key during delete
    private final OrdersRepository ordersRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepo;

    @Override
    public Client registerClient(RegisterFormDTO dto) {
        if (userRepo.existsByEmail(dto.getEmail())) throw new DuplicateEmailException(dto.getEmail());
        Client c = new Client();
        c.setName(dto.getName().trim());
        c.setEmail(dto.getEmail().trim().toLowerCase());
        c.setPassword(passwordEncoder.encode(dto.getPassword()));
        Client saved = clientRepo.save(c);
        log.info("Client registered: id={}, email='{}'", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    public Client processOAuth2PostLogin(String email, String name) {
        String normalizedEmail = email.toLowerCase().trim();
        if (!userRepo.existsByEmail(normalizedEmail)) {
            Client client = new Client();
            client.setEmail(normalizedEmail);
            client.setName((name != null && !name.isBlank()) ? name : normalizedEmail);
            client.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            Client saved = clientRepo.save(client);
            log.info("[OAUTH2] Registered new client: {} (name='{}')", normalizedEmail, saved.getName());
            return saved;
        }
        return (Client) userRepo.findByEmail(normalizedEmail).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepo.searchUsers(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> getAllClients(Pageable pageable) {
        return clientRepo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepo.findAll(pageable);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Employee createEmployee(RegisterFormDTO dto, String department) {
        if (userRepo.existsByEmail(dto.getEmail())) throw new DuplicateEmailException(dto.getEmail());
        Employee e = new Employee();
        e.setName(dto.getName().trim());
        e.setEmail(dto.getEmail().trim().toLowerCase());
        e.setPassword(passwordEncoder.encode(dto.getPassword()));
        e.setDepartment(department != null ? department.trim() : "General");
        Employee saved = employeeRepo.save(e);
        log.info("Employee created: id={}, email='{}'", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void updateDepartment(Long employeeId, String department) {
        Employee e = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException(employeeId));
        if ("ADMIN".equalsIgnoreCase(e.getDepartment())
                && !"ADMIN".equalsIgnoreCase(department)
                && employeeRepo.isLastAdmin(employeeId)) {
            throw new LastAdminException();
        }
        e.setDepartment(department != null ? department.trim() : "General");
        employeeRepo.save(e);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Client updateClient(Long id, ClientEditDTO dto) {
        Client client = clientRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        String newEmail = dto.getEmail().trim().toLowerCase();
        if (!client.getEmail().equals(newEmail) && userRepo.existsByEmail(newEmail)) {
            throw new DuplicateEmailException();
        }
        client.setName(dto.getName().trim());
        client.setEmail(newEmail);
        Client saved = clientRepo.save(client);
        log.info("Client updated: id={}", id);
        return saved;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        refreshTokenRepo.deleteAllByUserId(id);
        passwordResetTokenRepo.deleteAllByUserId(id);
        if (user instanceof Client) {
            List<Orders> clientOrders = ordersRepo.findByClientIdWithFullDetails(id);
            ordersRepo.deleteAll(clientOrders);
        }

        userRepo.delete(user);
        log.info("User deleted: id={}", id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(Long id) {
        Employee e = employeeRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if ("ADMIN".equalsIgnoreCase(e.getDepartment()) && employeeRepo.isLastAdmin(id)) {
            throw new LastAdminException(id);
        }

        refreshTokenRepo.deleteAllByUserId(id);
        passwordResetTokenRepo.deleteAllByUserId(id);

        List<Orders> empOrders = ordersRepo.findByEmployeeId(id);
        for (Orders o : empOrders) {
            o.setEmployee(null);
        }
        ordersRepo.saveAll(empOrders);

        employeeRepo.deleteById(id);
        log.info("Employee deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public long countClients() {
        return clientRepo.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countEmployees() {
        return employeeRepo.count();
    }
}