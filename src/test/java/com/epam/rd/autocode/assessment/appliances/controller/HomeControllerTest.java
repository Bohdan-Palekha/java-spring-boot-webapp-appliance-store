package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.model.*;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.OrdersService;
import com.epam.rd.autocode.assessment.appliances.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeController Unit Tests")
class HomeControllerTest {

    @Mock
    private ApplianceService applianceService;
    @Mock
    private OrdersService ordersService;
    @Mock
    private UserService userService;
    @InjectMocks
    private HomeController controller;

    private Model model;
    private Appliance washer;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        Manufacturer samsung = new Manufacturer(1L, "Samsung");
        washer = new Appliance(1L, "Washer", Category.BIG, "W1",
                samsung, PowerType.AC220, "", "", 2000, new BigDecimal("500"));
    }

    private Authentication clientAuth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }

    private Authentication adminAuth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    private Authentication employeeAuth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    @Test
    @DisplayName("accessDenied() returns 'error/403' with errorCode 403")
    void accessDenied_returnsCorrectViewAndModel() {
        String view = controller.accessDenied(model);

        assertThat(view).isEqualTo("error/403");
        assertThat(model.asMap().get("errorCode")).isEqualTo(403);
    }

    @Nested
    @DisplayName("home()")
    class HomeTests {

        @Test
        @DisplayName("returns 'index' view with featured appliances")
        void home_returnsIndexView() {
            given(applianceService.getFeatured(8)).willReturn(List.of(washer));

            String view = controller.home(model, null);

            assertThat(view).isEqualTo("index");
            assertThat(model.asMap()).containsKey("featured");
            assertThat(model.asMap()).containsKey("categories");
        }

        @Test
        @DisplayName("sets 'authenticated' = false for anonymous user")
        void home_anonymousUser_authenticatedFalse() {
            given(applianceService.getFeatured(8)).willReturn(List.of());

            controller.home(model, null);

            assertThat(model.asMap().get("authenticated")).isEqualTo(false);
        }

        @Test
        @DisplayName("sets 'authenticated' = true for logged-in client")
        void home_loggedInClient_authenticatedTrue() {
            given(applianceService.getFeatured(8)).willReturn(List.of());

            controller.home(model, clientAuth("alice@example.com"));

            assertThat(model.asMap().get("authenticated")).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("dashboard()")
    class DashboardTests {

        @Test
        @DisplayName("returns 'dashboard' view")
        void dashboard_returnsView() {
            String view = controller.dashboard(model, clientAuth("alice@example.com"));
            assertThat(view).isEqualTo("dashboard");
        }

        @Test
        @DisplayName("loads order stats for ADMIN role")
        void dashboard_adminRole_loadsOrderStats() {
            given(ordersService.countPending()).willReturn(3L);
            given(ordersService.countApproved()).willReturn(10L);
            given(ordersService.countRejected()).willReturn(2L);
            given(applianceService.countAll()).willReturn(50L);
            given(userService.countClients()).willReturn(20L);
            given(userService.countEmployees()).willReturn(5L);

            controller.dashboard(model, adminAuth("admin@store.com"));

            assertThat(model.asMap()).containsKeys(
                    "pendingOrders", "approvedOrders", "rejectedOrders",
                    "totalAppliances", "totalClients", "totalEmployees");
        }

        @Test
        @DisplayName("loads order stats but not user counts for EMPLOYEE role")
        void dashboard_employeeRole_loadsOrderStatsNotUserCounts() {
            given(ordersService.countPending()).willReturn(3L);
            given(ordersService.countApproved()).willReturn(10L);
            given(ordersService.countRejected()).willReturn(2L);
            given(applianceService.countAll()).willReturn(50L);

            controller.dashboard(model, employeeAuth("jane@store.com"));

            assertThat(model.asMap()).containsKeys("pendingOrders", "approvedOrders", "rejectedOrders");
            assertThat(model.asMap()).doesNotContainKey("totalClients");
            assertThat(model.asMap()).doesNotContainKey("totalEmployees");
        }

        @Test
        @DisplayName("loads myOrders for CLIENT role")
        void dashboard_clientRole_loadsMyOrders() {
            Client alice = new Client(1L, "Alice", "alice@example.com", "pw", null);
            Orders order = new Orders(1L, null, alice, null, new HashSet<>());
            given(ordersService.getOrdersForClient("alice@example.com")).willReturn(List.of(order));

            controller.dashboard(model, clientAuth("alice@example.com"));

            assertThat(model.asMap()).containsKey("myOrders");
            then(ordersService).should().getOrdersForClient("alice@example.com");
        }

        @Test
        @DisplayName("does not query order stats for CLIENT role")
        void dashboard_clientRole_doesNotLoadAdminStats() {
            given(ordersService.getOrdersForClient(any())).willReturn(List.of());

            controller.dashboard(model, clientAuth("alice@example.com"));

            then(ordersService).should(never()).countPending();
            then(ordersService).should(never()).countApproved();
        }
    }
}
