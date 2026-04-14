package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.controller.admin.AdminOrdersController;
import com.epam.rd.autocode.assessment.appliances.exception.IllegalOrderStateException;
import com.epam.rd.autocode.assessment.appliances.exception.OrderNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.Orders;
import com.epam.rd.autocode.assessment.appliances.service.OrdersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminOrdersController Unit Tests")
class AdminOrdersControllerTest {

    @Mock
    private OrdersService ordersService;
    @InjectMocks
    private AdminOrdersController controller;

    private Model model;
    private Authentication employeeAuth;
    private Client alice;
    private Orders pendingOrder;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        employeeAuth = new UsernamePasswordAuthenticationToken("jane@store.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        alice = new Client(1L, "Alice", "alice@example.com", "pw", null);
        pendingOrder = new Orders(1L, null, alice, null, new HashSet<>());
    }

    @Test
    @DisplayName("detail() returns 'admin/orders/detail' with order in model")
    void detail_found_returnsDetailView() {
        given(ordersService.getOrderById(1L)).willReturn(pendingOrder);

        String view = controller.detail(1L, model);

        assertThat(view).isEqualTo("admin/orders/detail");
        assertThat(model.asMap().get("order")).isEqualTo(pendingOrder);
    }

    @Test
    @DisplayName("detail() propagates OrderNotFoundException when not found")
    void detail_notFound_throwsException() {
        given(ordersService.getOrderById(99L)).willThrow(new OrderNotFoundException(99L));

        assertThatThrownBy(() -> controller.detail(99L, model))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("returns 'admin/orders/list' with all orders when no filter")
        void list_noFilter_returnsAllOrders() {
            given(ordersService.getAllOrders(any())).willReturn(new PageImpl<>(List.of(pendingOrder)));
            given(ordersService.countPending()).willReturn(1L);
            given(ordersService.countApproved()).willReturn(0L);
            given(ordersService.countRejected()).willReturn(0L);

            String view = controller.list(null, 0, 20, model);

            assertThat(view).isEqualTo("admin/orders/list");
            assertThat(model.asMap()).containsKeys("orders", "totalPages", "currentPage");
            then(ordersService).should().getAllOrders(any());
            then(ordersService).should(never()).getByApprovalStatus(any(), any());
        }

        @Test
        @DisplayName("delegates to getByApprovalStatus when filter is specified")
        void list_withFilter_delegatesToFilteredMethod() {
            given(ordersService.getByApprovalStatus(eq(true), any()))
                    .willReturn(new PageImpl<>(List.of()));
            given(ordersService.countPending()).willReturn(0L);
            given(ordersService.countApproved()).willReturn(0L);
            given(ordersService.countRejected()).willReturn(0L);

            String view = controller.list(true, 0, 20, model);

            assertThat(view).isEqualTo("admin/orders/list");
            then(ordersService).should().getByApprovalStatus(true, PageRequest.of(0, 20, Sort.by("id").descending()));
            then(ordersService).should(never()).getAllOrders(any());
        }

        @Test
        @DisplayName("adds counts (pending/approved/rejected) to model")
        void list_addsCounts() {
            given(ordersService.getAllOrders(any())).willReturn(Page.empty());
            given(ordersService.countPending()).willReturn(5L);
            given(ordersService.countApproved()).willReturn(10L);
            given(ordersService.countRejected()).willReturn(3L);

            controller.list(null, 0, 20, model);

            assertThat(model.asMap().get("pendingCount")).isEqualTo(5L);
            assertThat(model.asMap().get("approvedCount")).isEqualTo(10L);
            assertThat(model.asMap().get("rejectedCount")).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("approve()")
    class ApproveTests {

        @Test
        @DisplayName("calls setApprovalStatus(true) and redirects to order detail")
        void approve_success_redirectsToDetail() {
            given(ordersService.setApprovalStatus(1L, true, "jane@store.com")).willReturn(pendingOrder);
            var ra = new RedirectAttributesModelMap();

            String view = controller.approve(1L, employeeAuth, ra);

            assertThat(view).isEqualTo("redirect:/admin/orders/1");
            then(ordersService).should().setApprovalStatus(1L, true, "jane@store.com");
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("propagates IllegalOrderStateException for already-processed order")
        void approve_alreadyProcessed_throwsException() {
            willThrow(new IllegalOrderStateException(1L))
                    .given(ordersService).setApprovalStatus(1L, true, "jane@store.com");

            assertThatThrownBy(() -> controller.approve(1L, employeeAuth, new RedirectAttributesModelMap()))
                    .isInstanceOf(IllegalOrderStateException.class);
        }
    }

    @Nested
    @DisplayName("reject()")
    class RejectTests {

        @Test
        @DisplayName("calls setApprovalStatus(false) and redirects to order detail")
        void reject_success_redirectsToDetail() {
            given(ordersService.setApprovalStatus(1L, false, "jane@store.com")).willReturn(pendingOrder);
            var ra = new RedirectAttributesModelMap();

            String view = controller.reject(1L, employeeAuth, ra);

            assertThat(view).isEqualTo("redirect:/admin/orders/1");
            then(ordersService).should().setApprovalStatus(1L, false, "jane@store.com");
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }
    }
}
