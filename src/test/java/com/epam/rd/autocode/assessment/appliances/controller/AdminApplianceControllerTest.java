package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.controller.admin.AdminApplianceController;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceFormDTO;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminApplianceController Unit Tests")
class AdminApplianceControllerTest {

    @Mock
    private ApplianceService applianceService;
    @Mock
    private ManufacturerService manufacturerService;
    @InjectMocks
    private AdminApplianceController controller;

    private Model model;
    private Manufacturer samsung;
    private Appliance washer;
    private ApplianceFormDTO dto;
    private BindingResult noErrors;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        samsung = new Manufacturer(1L, "Samsung");
        washer = new Appliance(1L, "Washer", Category.BIG, "W1",
                samsung, PowerType.AC220, "", "", 2000, new BigDecimal("500"));
        dto = new ApplianceFormDTO();
        dto.setName("Washer");
        dto.setCategory(Category.BIG);
        dto.setModel("W1");
        dto.setManufacturerId(1L);
        dto.setPowerType(PowerType.AC220);
        dto.setPower(2000);
        dto.setPrice(new BigDecimal("500"));
        noErrors = new BeanPropertyBindingResult(dto, "form");
    }

    @Test
    @DisplayName("list() returns 'admin/appliances/list' with pagination model")
    void list_returnsView() {
        given(applianceService.getAll(any())).willReturn(new PageImpl<>(List.of(washer)));

        String view = controller.list(0, 20, "id", "desc", model);

        assertThat(view).isEqualTo("admin/appliances/list");
        assertThat(model.asMap()).containsKeys("appliances", "totalPages", "currentPage", "sortBy", "sortDir");
    }

    @Test
    @DisplayName("createForm() returns 'admin/appliances/form' with empty DTO")
    void createForm_returnsFormView() {
        given(manufacturerService.getAllManufacturers()).willReturn(List.of(samsung));

        String view = controller.createForm(model);

        assertThat(view).isEqualTo("admin/appliances/form");
        assertThat(model.asMap()).containsKey("form");
    }

    @Test
    @DisplayName("editForm() returns 'admin/appliances/form' with populated DTO")
    void editForm_found_returnsFormView() {
        given(applianceService.getById(1L)).willReturn(washer);
        given(manufacturerService.getAllManufacturers()).willReturn(List.of(samsung));

        String view = controller.editForm(1L, model);

        assertThat(view).isEqualTo("admin/appliances/form");
        assertThat(model.asMap()).containsKey("form");
        assertThat(model.asMap()).containsKey("applianceId");
    }

    @Test
    @DisplayName("delete() calls service and redirects to /admin/appliances")
    void delete_success_redirectsToList() {
        var ra = new RedirectAttributesModelMap();

        String view = controller.delete(1L, ra);

        assertThat(view).isEqualTo("redirect:/admin/appliances");
        then(applianceService).should().delete(1L);
        assertThat(ra.getFlashAttributes()).containsKey("successMessage");
    }

    @Test
    @DisplayName("delete() propagates exception when appliance not found")
    void delete_notFound_throwsException() {
        willThrow(new ApplianceNotFoundException(99L)).given(applianceService).delete(99L);

        assertThatThrownBy(() -> controller.delete(99L, new RedirectAttributesModelMap()))
                .isInstanceOf(ApplianceNotFoundException.class);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("redirects to /admin/appliances on success")
        void create_valid_redirectsToList() {
            given(applianceService.create(dto)).willReturn(washer);
            var ra = new RedirectAttributesModelMap();

            String view = controller.create(dto, noErrors, model, ra);

            assertThat(view).isEqualTo("redirect:/admin/appliances");
            then(applianceService).should().create(dto);
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("returns form view when binding errors exist")
        void create_bindingErrors_returnsFormView() {
            given(manufacturerService.getAllManufacturers()).willReturn(List.of(samsung));
            BindingResult errors = new BeanPropertyBindingResult(dto, "form");
            errors.rejectValue("name", "error", "required");

            String view = controller.create(dto, errors, model, new RedirectAttributesModelMap());

            assertThat(view).isEqualTo("admin/appliances/form");
            then(applianceService).should(never()).create(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("redirects to /admin/appliances on success")
        void update_valid_redirectsToList() {
            given(applianceService.update(1L, dto)).willReturn(washer);
            var ra = new RedirectAttributesModelMap();

            String view = controller.update(1L, dto, noErrors, model, ra);

            assertThat(view).isEqualTo("redirect:/admin/appliances");
            then(applianceService).should().update(1L, dto);
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("returns form view when binding errors exist")
        void update_bindingErrors_returnsFormView() {
            given(manufacturerService.getAllManufacturers()).willReturn(List.of(samsung));
            BindingResult errors = new BeanPropertyBindingResult(dto, "form");
            errors.rejectValue("name", "error", "required");

            String view = controller.update(1L, dto, errors, model, new RedirectAttributesModelMap());

            assertThat(view).isEqualTo("admin/appliances/form");
            then(applianceService).should(never()).update(anyLong(), any());
        }
    }
}
