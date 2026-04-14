package com.epam.rd.autocode.assessment.appliances.service;

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
import com.epam.rd.autocode.assessment.appliances.service.impl.OrdersServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdersService Unit Tests")
class OrdersServiceTest {

    @Mock
    private OrdersRepository ordersRepo;
    @Mock
    private ApplianceRepository applianceRepo;
    @Mock
    private ClientRepository clientRepo;
    @Mock
    private EmployeeRepository employeeRepo;
    @InjectMocks
    private OrdersServiceImpl service;

    @Captor
    private ArgumentCaptor<Orders> ordersCaptor;

    private Client alice;
    private Client bob;
    private Employee jane;
    private Appliance washer;
    private OrderFormDTO dto;

    @BeforeEach
    void setUp() {
        Manufacturer samsung = new Manufacturer(1L, "Samsung");
        alice = new Client(1L, "Alice", "alice@example.com", "$2a$12$hash", "CARD-001");
        bob = new Client(2L, "Bob", "bob@example.com", "$2a$12$hash", "CARD-002");
        jane = new Employee(3L, "Jane", "jane@store.com", "$2a$12$hash", "Sales");
        washer = new Appliance(1L, "Samsung Washer", Category.BIG, "WW90T",
                samsung, PowerType.AC220, "9kg", "Front loader", 2000, new BigDecimal("799.99"));

        dto = new OrderFormDTO();
        dto.setApplianceId(1L);
        dto.setQuantity(2L);
    }

    @Test
    @DisplayName("getOrdersForClient loads only this client's orders")
    void getOrdersForClient_loadsOnlyClientOrders() {
        Orders o1 = new Orders(1L, null, alice, null, new HashSet<>());
        Orders o2 = new Orders(2L, true, alice, jane, new HashSet<>());

        given(clientRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
        given(ordersRepo.findByClientIdWithFullDetails(alice.getId())).willReturn(List.of(o1, o2));

        List<Orders> result = service.getOrdersForClient("alice@example.com");
        assertThat(result).hasSize(2);

        verify(ordersRepo).findByClientIdWithFullDetails(alice.getId());
        verify(ordersRepo, never()).findByClientIdWithFullDetails(bob.getId());
    }

    @Nested
    @DisplayName("placeOrder()")
    class PlaceOrderTests {

        @Test
        @DisplayName("creates order with correct price snapshot")
        void validRequest_createsOrderWithPriceSnapshot() {
            given(clientRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            given(ordersRepo.save(any(Orders.class))).willAnswer(inv -> {
                Orders o = inv.getArgument(0);
                return new Orders(42L, null, o.getClient(), null, o.getOrderRowSet());
            });

            service.placeOrder("alice@example.com", dto);

            verify(ordersRepo).save(ordersCaptor.capture());
            Orders captured = ordersCaptor.getValue();

            assertThat(captured.getApproved()).isNull();        // PENDING state
            assertThat(captured.getClient()).isEqualTo(alice);
            assertThat(captured.getEmployee()).isNull();         // not yet assigned
            assertThat(captured.getOrderRowSet()).hasSize(1);

            OrderRow row = captured.getOrderRowSet().iterator().next();
            assertThat(row.getAppliance()).isEqualTo(washer);
            assertThat(row.getNumber()).isEqualTo(2L);
            // 799.99 × 2 = 1599.98
            assertThat(row.getAmount()).isEqualByComparingTo(new BigDecimal("1599.98"));
        }

        @Test
        @DisplayName("price snapshot captures price at order time")
        void priceSnapshot_capturedAtOrderTime() {
            given(clientRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            given(ordersRepo.save(any(Orders.class))).willAnswer(inv -> inv.getArgument(0));

            dto.setQuantity(1L);
            service.placeOrder("alice@example.com", dto);

            verify(ordersRepo).save(ordersCaptor.capture());
            OrderRow row = ordersCaptor.getValue().getOrderRowSet().iterator().next();
            assertThat(row.getAmount()).isEqualByComparingTo(new BigDecimal("799.99"));

            // Changing price after order does NOT affect stored snapshot
            washer.setPrice(new BigDecimal("999.99"));
            assertThat(row.getAmount()).isEqualByComparingTo(new BigDecimal("799.99"));
        }

        @Test
        @DisplayName("quantity 100 computes correct total")
        void largeQuantity_computesCorrectTotal() {
            dto.setQuantity(100L);
            given(clientRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            given(ordersRepo.save(any(Orders.class))).willAnswer(inv -> inv.getArgument(0));

            service.placeOrder("alice@example.com", dto);

            verify(ordersRepo).save(ordersCaptor.capture());
            OrderRow row = ordersCaptor.getValue().getOrderRowSet().iterator().next();
            assertThat(row.getAmount()).isEqualByComparingTo(new BigDecimal("79999.00"));
        }

        @Test
        @DisplayName("unknown client email throws UserNotFoundException")
        void unknownClient_throwsUserNotFound() {
            given(clientRepo.findByEmail("unknown@example.com")).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.placeOrder("unknown@example.com", dto))
                    .isInstanceOf(UserNotFoundException.class);
            verify(ordersRepo, never()).save(any());
        }

        @Test
        @DisplayName("non-existent appliance throws ApplianceNotFoundException")
        void nonExistentAppliance_throwsApplianceNotFound() {
            given(clientRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            dto.setApplianceId(99L);
            given(applianceRepo.findById(99L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.placeOrder("alice@example.com", dto))
                    .isInstanceOf(ApplianceNotFoundException.class);
            verify(ordersRepo, never()).save(any());
        }

        @Test
        @DisplayName("client is loaded from auth context, never from request body")
        void clientFromAuthContext() {
            given(clientRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            given(ordersRepo.save(any(Orders.class))).willAnswer(inv -> inv.getArgument(0));

            service.placeOrder("alice@example.com", dto);

            // Client must be loaded by the email from authentication, not from DTO
            verify(clientRepo).findByEmail("alice@example.com");
        }
    }

    // ── getOrderForClient (ownership check) ───────────────────────────────────

    @Nested
    @DisplayName("setApprovalStatus()")
    class SetApprovalStatusTests {

        private Orders pendingOrder;
        private Orders approvedOrder;
        private Orders rejectedOrder;

        @BeforeEach
        void setUpOrders() {
            pendingOrder = new Orders(1L, null, alice, null, new HashSet<>());
            approvedOrder = new Orders(2L, true, alice, jane, new HashSet<>());
            rejectedOrder = new Orders(3L, false, alice, jane, new HashSet<>());
        }

        @Test
        @DisplayName("PENDING -> APPROVED succeeds and records employee")
        void approvePendingOrder_succeeds() {
            given(ordersRepo.findByIdWithFullDetails(1L)).willReturn(Optional.of(pendingOrder));
            given(employeeRepo.findByEmail("jane@store.com")).willReturn(Optional.of(jane));
            given(ordersRepo.save(pendingOrder)).willReturn(pendingOrder);

            service.setApprovalStatus(1L, true, "jane@store.com");

            assertThat(pendingOrder.getApproved()).isTrue();
            assertThat(pendingOrder.getEmployee()).isEqualTo(jane);
            verify(ordersRepo).save(pendingOrder);
        }

        @Test
        @DisplayName("PENDING -> REJECTED succeeds")
        void rejectPendingOrder_succeeds() {
            given(ordersRepo.findByIdWithFullDetails(1L)).willReturn(Optional.of(pendingOrder));
            given(employeeRepo.findByEmail("jane@store.com")).willReturn(Optional.of(jane));
            given(ordersRepo.save(pendingOrder)).willReturn(pendingOrder);

            service.setApprovalStatus(1L, false, "jane@store.com");

            assertThat(pendingOrder.getApproved()).isFalse();
        }

        @Test
        @DisplayName("APPROVED -> REJECTED throws IllegalOrderStateException")
        void approvedToRejected_throwsIllegalState() {
            given(ordersRepo.findByIdWithFullDetails(2L)).willReturn(Optional.of(approvedOrder));
            assertThatThrownBy(() -> service.setApprovalStatus(2L, false, "jane@store.com"))
                    .isInstanceOf(IllegalOrderStateException.class);
            verify(ordersRepo, never()).save(any());
        }

        @Test
        @DisplayName("REJECTED -> APPROVED throws IllegalOrderStateException")
        void rejectedToApproved_throwsIllegalState() {
            given(ordersRepo.findByIdWithFullDetails(3L)).willReturn(Optional.of(rejectedOrder));
            assertThatThrownBy(() -> service.setApprovalStatus(3L, true, "jane@store.com"))
                    .isInstanceOf(IllegalOrderStateException.class);
            verify(ordersRepo, never()).save(any());
        }

        @Test
        @DisplayName("non-existent order throws OrderNotFoundException")
        void nonExistentOrder_throwsOrderNotFound() {
            given(ordersRepo.findByIdWithFullDetails(999L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.setApprovalStatus(999L, true, "jane@store.com"))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("state check happens BEFORE employee lookup")
        void stateCheckedBeforeEmployeeLookup() {
            given(ordersRepo.findByIdWithFullDetails(2L)).willReturn(Optional.of(approvedOrder));

            assertThatThrownBy(() -> service.setApprovalStatus(2L, false, "jane@store.com"))
                    .isInstanceOf(IllegalOrderStateException.class);

            verify(employeeRepo, never()).findByEmail(any());
        }

        @Test
        @DisplayName("employee email is from authentication context")
        void employeeLoadedByAuthEmail() {
            given(ordersRepo.findByIdWithFullDetails(1L)).willReturn(Optional.of(pendingOrder));
            given(employeeRepo.findByEmail("jane@store.com")).willReturn(Optional.of(jane));
            given(ordersRepo.save(pendingOrder)).willReturn(pendingOrder);

            service.setApprovalStatus(1L, true, "jane@store.com");

            verify(employeeRepo).findByEmail("jane@store.com");
        }
    }

    @Nested
    @DisplayName("getOrderForClient() — Ownership")
    class OwnershipTests {

        @Test
        @DisplayName("client can access their own order")
        void client_canAccessOwnOrder() {
            Orders aliceOrder = new Orders(1L, null, alice, null, new HashSet<>());
            given(clientRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(ordersRepo.findByIdAndClientIdWithDetails(1L, alice.getId()))
                    .willReturn(Optional.of(aliceOrder));

            assertThatCode(() -> service.getOrderForClient(1L, "alice@example.com"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("wrong client gets 404, not 403 (prevents ID enumeration)")
        void wrongClient_gets404NotAccessDenied() {
            given(clientRepo.findByEmail("bob@example.com")).willReturn(Optional.of(bob));
            given(ordersRepo.findByIdAndClientIdWithDetails(1L, bob.getId()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> service.getOrderForClient(1L, "bob@example.com"))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("query uses BOTH orderId AND clientId for ownership check")
        void ownershipCheckUsesBothIds() {
            given(clientRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(ordersRepo.findByIdAndClientIdWithDetails(any(), any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.getOrderForClient(5L, "alice@example.com"))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(ordersRepo).findByIdAndClientIdWithDetails(5L, alice.getId());
        }
    }


    @Nested
    @DisplayName("Order statistics")
    class StatsTests {

        @Test
        void countPending_delegatesToRepo() {
            given(ordersRepo.countByApproved(null)).willReturn(5L);
            assertThat(service.countPending()).isEqualTo(5L);
            verify(ordersRepo).countByApproved(null);
        }

        @Test
        void countApproved_delegatesToRepo() {
            given(ordersRepo.countByApproved(Boolean.TRUE)).willReturn(10L);
            assertThat(service.countApproved()).isEqualTo(10L);
            verify(ordersRepo).countByApproved(Boolean.TRUE);
        }

        @Test
        void countRejected_delegatesToRepo() {
            given(ordersRepo.countByApproved(Boolean.FALSE)).willReturn(3L);
            assertThat(service.countRejected()).isEqualTo(3L);
            verify(ordersRepo).countByApproved(Boolean.FALSE);
        }

        @Test
        @DisplayName("all three status counts are independent queries")
        void allCounts_useDistinctQueries() {
            given(ordersRepo.countByApproved(null)).willReturn(5L);
            given(ordersRepo.countByApproved(Boolean.TRUE)).willReturn(10L);
            given(ordersRepo.countByApproved(Boolean.FALSE)).willReturn(3L);

            long total = service.countPending() + service.countApproved() + service.countRejected();
            assertThat(total).isEqualTo(18L);

            verify(ordersRepo).countByApproved(null);
            verify(ordersRepo).countByApproved(Boolean.TRUE);
            verify(ordersRepo).countByApproved(Boolean.FALSE);
        }
    }
}
