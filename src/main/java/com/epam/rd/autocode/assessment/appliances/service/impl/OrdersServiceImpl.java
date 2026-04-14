package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.OrderFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ApplianceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.exception.IllegalOrderStateException;
import com.epam.rd.autocode.assessment.appliances.exception.OrderNotFoundException;
import com.epam.rd.autocode.assessment.appliances.exception.UserNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.*;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import com.epam.rd.autocode.assessment.appliances.repository.EmployeeRepository;
import com.epam.rd.autocode.assessment.appliances.repository.OrdersRepository;
import com.epam.rd.autocode.assessment.appliances.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepo;
    private final ApplianceRepository applianceRepo;
    private final ClientRepository clientRepo;
    private final EmployeeRepository employeeRepo;

    @Override
    public Orders placeOrder(String clientEmail, OrderFormDTO dto) {
        Client client = clientRepo.findByEmail(clientEmail)
                .orElseThrow(() -> new UserNotFoundException(clientEmail));
        Appliance appliance = applianceRepo.findById(dto.getApplianceId())
                .orElseThrow(() -> new ApplianceNotFoundException(dto.getApplianceId()));

        BigDecimal lineTotal = appliance.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        OrderRow row = new OrderRow(null, appliance, lineTotal, dto.getQuantity());

        Orders order = new Orders();
        order.setClient(client);
        order.setApproved(null);
        order.addOrderRow(row);

        Orders saved = ordersRepo.save(order);
        log.info("Order placed: id={}, client='{}', appliance={}", saved.getId(), clientEmail, appliance.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Orders> getOrdersForClient(String clientEmail) {
        Client client = clientRepo.findByEmail(clientEmail)
                .orElseThrow(() -> new UserNotFoundException(clientEmail));
        return ordersRepo.findByClientIdWithFullDetails(client.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Orders getOrderForClient(Long orderId, String clientEmail) {
        Client client = clientRepo.findByEmail(clientEmail)
                .orElseThrow(() -> new UserNotFoundException(clientEmail));
        return ordersRepo.findByIdAndClientIdWithDetails(orderId, client.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Page<Orders> getAllOrders(Pageable pageable) {
        return ordersRepo.findAllWithDetails(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Page<Orders> getByApprovalStatus(Boolean approved, Pageable pageable) {
        return ordersRepo.findByApprovedStatus(approved, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Orders getOrderById(Long orderId) {
        return ordersRepo.findByIdWithFullDetails(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Orders setApprovalStatus(Long orderId, boolean approve, String employeeEmail) {
        Orders order = ordersRepo.findByIdWithFullDetails(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (order.getApproved() != null) throw new IllegalOrderStateException(orderId);
        Employee emp = employeeRepo.findByEmail(employeeEmail)
                .orElseThrow(() -> new UserNotFoundException(employeeEmail));
        order.setApproved(approve);
        order.setEmployee(emp);
        Orders saved = ordersRepo.save(order);
        log.info("Order {} {}: id={}, by='{}'", approve ? "approved" : "rejected", "", orderId, employeeEmail);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public long countPending() {
        return ordersRepo.countByApproved(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countApproved() {
        return ordersRepo.countByApproved(Boolean.TRUE);
    }

    @Override
    @Transactional(readOnly = true)
    public long countRejected() {
        return ordersRepo.countByApproved(Boolean.FALSE);
    }
}
