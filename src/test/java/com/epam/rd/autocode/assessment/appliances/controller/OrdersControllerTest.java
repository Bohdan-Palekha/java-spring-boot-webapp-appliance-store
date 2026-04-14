package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.OrderFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ApplianceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.exception.OrderNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.*;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.OrdersService;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdersController Unit Tests")
class OrdersControllerTest {

    @Mock
    private OrdersService ordersService;
    @Mock
    private ApplianceService applianceService;
    @InjectMocks
    private OrdersController controller;

    private Model model;
    private Authentication clientAuth;
    private Client alice;
    private Appliance washer;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        clientAuth = new UsernamePasswordAuthenticationToken("alice@example.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        alice = new Client(1L, "Alice", "alice@example.com", "pw", null);
        Manufacturer samsung = new Manufacturer(1L, "Samsung");
        washer = new Appliance(1L, "Washer", Category.BIG, "W1",
                samsung, PowerType.AC220, "", "", 2000, new BigDecimal("500"));
    }

    @Test
    @DisplayName("myOrders() returns 'orders/list' with orders in model")
    void myOrders_returnsListView() {
        Orders order = new Orders(1L, null, alice, null, new HashSet<>());
        given(ordersService.getOrdersForClient("alice@example.com")).willReturn(List.of(order));

        String view = controller.myOrders(clientAuth, model);

        assertThat(view).isEqualTo("orders/list");
        assertThat(model.asMap()).containsKey("orders");
        then(ordersService).should().getOrdersForClient("alice@example.com");
    }

    @Nested
    @DisplayName("detail()")
    class DetailTests {

        @Test
        @DisplayName("returns 'orders/detail' with order in model when authorized")
        void detail_ownOrder_returnsDetailView() {
            Orders order = new Orders(1L, null, alice, null, new HashSet<>());
            given(ordersService.getOrderForClient(1L, "alice@example.com")).willReturn(order);

            String view = controller.detail(1L, clientAuth, model);

            assertThat(view).isEqualTo("orders/detail");
            assertThat(model.asMap().get("order")).isEqualTo(order);
        }

        @Test
        @DisplayName("propagates OrderNotFoundException for wrong owner")
        void detail_wrongOwner_throwsNotFoundException() {
            given(ordersService.getOrderForClient(1L, "alice@example.com"))
                    .willThrow(new OrderNotFoundException(1L));

            assertThatThrownBy(() -> controller.detail(1L, clientAuth, model))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }


    @Nested
    @DisplayName("showPlaceForm()")
    class ShowPlaceFormTests {

        @Test
        @DisplayName("returns 'orders/create' and adds empty form when no applianceId")
        void showPlaceForm_noApplianceId_returnsCreateView() {
            String view = controller.showPlaceForm(null, model);

            assertThat(view).isEqualTo("orders/create");
            assertThat(model.asMap()).containsKey("orderForm");
        }

        @Test
        @DisplayName("pre-populates selectedAppliance when valid applianceId provided")
        void showPlaceForm_withApplianceId_addsAppliance() {
            given(applianceService.getById(1L)).willReturn(washer);

            controller.showPlaceForm(1L, model);

            assertThat(model.asMap().get("selectedAppliance")).isEqualTo(washer);
        }

        @Test
        @DisplayName("does not fail when applianceId is not found — silently ignored")
        void showPlaceForm_invalidApplianceId_silentlyIgnored() {
            given(applianceService.getById(99L)).willThrow(new ApplianceNotFoundException(99L));

            assertThatCode(() -> controller.showPlaceForm(99L, model))
                    .doesNotThrowAnyException();
            assertThat(model.asMap()).doesNotContainKey("selectedAppliance");
        }
    }

    @Nested
    @DisplayName("placeOrder()")
    class PlaceOrderTests {

        private OrderFormDTO dto;
        private BindingResult noErrors;

        @BeforeEach
        void setupDto() {
            dto = new OrderFormDTO();
            dto.setApplianceId(1L);
            dto.setQuantity(2L);
            noErrors = new BeanPropertyBindingResult(dto, "orderForm");
        }

        @Test
        @DisplayName("places order and redirects to /orders on success")
        void placeOrder_validDto_redirectsToOrders() {
            Orders saved = new Orders(10L, null, alice, null, new HashSet<>());
            given(ordersService.placeOrder("alice@example.com", dto)).willReturn(saved);

            var ra = new RedirectAttributesModelMap();
            String view = controller.placeOrder(dto, noErrors, clientAuth, ra, model);

            assertThat(view).isEqualTo("redirect:/orders");
            then(ordersService).should().placeOrder("alice@example.com", dto);
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("returns 'orders/create' when binding errors exist")
        void placeOrder_bindingErrors_returnsCreateForm() {
            BindingResult errors = new BeanPropertyBindingResult(dto, "orderForm");
            errors.rejectValue("quantity", "error", "Quantity required");

            String view = controller.placeOrder(dto, errors, clientAuth,
                    new RedirectAttributesModelMap(), model);

            assertThat(view).isEqualTo("orders/create");
            then(ordersService).should(never()).placeOrder(any(), any());
        }

        @Test
        @DisplayName("adds selectedAppliance to model on validation error when applianceId present")
        void placeOrder_bindingErrorsWithApplianceId_addsApplianceToModel() {
            given(applianceService.getById(1L)).willReturn(washer);
            BindingResult errors = new BeanPropertyBindingResult(dto, "orderForm");
            errors.rejectValue("quantity", "error", "Quantity required");

            controller.placeOrder(dto, errors, clientAuth,
                    new RedirectAttributesModelMap(), model);

            assertThat(model.asMap().get("selectedAppliance")).isEqualTo(washer);
        }
    }
}
