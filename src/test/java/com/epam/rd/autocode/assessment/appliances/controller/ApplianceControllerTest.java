package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.exception.ApplianceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplianceController Unit Tests")
class ApplianceControllerTest {

    @Mock
    private ApplianceService applianceService;
    @Mock
    private ManufacturerService manufacturerService;
    @InjectMocks
    private ApplianceController controller;

    private Model model;
    private Manufacturer samsung;
    private Appliance washer;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        samsung = new Manufacturer(1L, "Samsung");
        washer = new Appliance(1L, "Samsung Washer", Category.BIG, "WW90T",
                samsung, PowerType.AC220, "9kg", "Front loader", 2000, new BigDecimal("799.99"));
    }

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("returns 'appliances/list' view")
        void list_returnsCorrectView() {
            given(applianceService.search(any(), any(), any(), any(), any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of(washer)));
            given(manufacturerService.getAllManufacturers()).willReturn(List.of(samsung));

            String view = controller.list(null, null, null, null, null, null,
                    0, 12, "name", "asc", model);

            assertThat(view).isEqualTo("appliances/list");
        }

        @Test
        @DisplayName("populates model with appliances, pagination, and filter attributes")
        void list_populatesModelCorrectly() {
            given(applianceService.search(any(), any(), any(), any(), any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of(washer)));
            given(manufacturerService.getAllManufacturers()).willReturn(List.of(samsung));

            controller.list("washer", Category.BIG, PowerType.AC220,
                    new BigDecimal("100"), new BigDecimal("900"), 1L,
                    0, 12, "price", "asc", model);

            assertThat(model.asMap()).containsKey("appliances");
            assertThat(model.asMap()).containsKey("totalPages");
            assertThat(model.asMap()).containsKey("currentPage");
            assertThat(model.asMap()).containsKey("categories");
            assertThat(model.asMap()).containsKey("powerTypes");
            assertThat(model.asMap()).containsKey("manufacturers");
            assertThat(model.asMap().get("keyword")).isEqualTo("washer");
            assertThat(model.asMap().get("hasFilters")).isEqualTo(true);
        }

        @Test
        @DisplayName("hasFilters is false when no filters applied")
        void list_noFilters_hasFiltersFalse() {
            given(applianceService.search(any(), any(), any(), any(), any(), any(), any()))
                    .willReturn(Page.empty());
            given(manufacturerService.getAllManufacturers()).willReturn(List.of());

            controller.list(null, null, null, null, null, null,
                    0, 12, "name", "asc", model);

            assertThat(model.asMap().get("hasFilters")).isEqualTo(false);
        }

        @Test
        @DisplayName("sanitizes invalid sortBy to 'name'")
        void list_invalidSortBy_sanitizedToName() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            given(applianceService.search(any(), any(), any(), any(), any(), any(), pageableCaptor.capture()))
                    .willReturn(Page.empty());
            given(manufacturerService.getAllManufacturers()).willReturn(List.of());

            controller.list(null, null, null, null, null, null,
                    0, 12, "INVALID_SORT", "asc", model);

            Pageable usedPageable = pageableCaptor.getValue();
            assertThat(usedPageable.getSort().getOrderFor("name")).isNotNull();
        }

        @Test
        @DisplayName("trims blank keyword before passing to service")
        void list_blankKeyword_passedAsNull() {
            ArgumentCaptor<String> kwCaptor = ArgumentCaptor.forClass(String.class);
            given(applianceService.search(kwCaptor.capture(), any(), any(), any(), any(), any(), any()))
                    .willReturn(Page.empty());
            given(manufacturerService.getAllManufacturers()).willReturn(List.of());

            controller.list("   ", null, null, null, null, null,
                    0, 12, "name", "asc", model);

            assertThat(kwCaptor.getValue()).isNull();
        }
    }

    @Nested
    @DisplayName("detail()")
    class DetailTests {

        @Test
        @DisplayName("returns 'appliances/detail' and adds appliance to model")
        void detail_found_returnsDetailView() {
            given(applianceService.getById(1L)).willReturn(washer);

            String view = controller.detail(1L, model);

            assertThat(view).isEqualTo("appliances/detail");
            assertThat(model.asMap().get("appliance")).isEqualTo(washer);
        }

        @Test
        @DisplayName("propagates ApplianceNotFoundException when not found")
        void detail_notFound_throwsException() {
            given(applianceService.getById(99L)).willThrow(new ApplianceNotFoundException(99L));

            assertThatThrownBy(() -> controller.detail(99L, model))
                    .isInstanceOf(ApplianceNotFoundException.class);
        }
    }
}
