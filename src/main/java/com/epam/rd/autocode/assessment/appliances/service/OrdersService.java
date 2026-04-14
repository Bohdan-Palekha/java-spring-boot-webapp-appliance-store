package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.OrderFormDTO;
import com.epam.rd.autocode.assessment.appliances.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrdersService {
    Orders placeOrder(String clientEmail, OrderFormDTO dto);

    List<Orders> getOrdersForClient(String clientEmail);

    Orders getOrderForClient(Long orderId, String clientEmail);

    Page<Orders> getAllOrders(Pageable pageable);

    Page<Orders> getByApprovalStatus(Boolean approved, Pageable pageable);

    Orders getOrderById(Long orderId);

    Orders setApprovalStatus(Long orderId, boolean approve, String employeeEmail);

    long countPending();

    long countApproved();

    long countRejected();
}
