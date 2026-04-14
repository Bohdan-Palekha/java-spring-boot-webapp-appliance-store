package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.controller.admin.AdminManufacturerController;
import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerHasAppliancesException;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminManufacturerController Unit Tests")
class AdminManufacturerControllerTest {

    @Mock
    private ManufacturerService manufacturerService;
    @InjectMocks
    private AdminManufacturerController controller;

    private Model model;
    private Manufacturer samsung;
    private ManufacturerFormDTO dto;
    private BindingResult noErrors;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        samsung = new Manufacturer(1L, "Samsung");
        dto = new ManufacturerFormDTO();
        dto.setName("Samsung");
        noErrors = new BeanPropertyBindingResult(dto, "form");
    }

    @Test
    @DisplayName("list() returns 'admin/manufacturers/list' with pagination model")
    void list_returnsView() {
        given(manufacturerService.getPaginated(any())).willReturn(new PageImpl<>(List.of(samsung)));

        String view = controller.list(0, 20, model);

        assertThat(view).isEqualTo("admin/manufacturers/list");
        assertThat(model.asMap()).containsKeys("manufacturers", "totalPages", "currentPage");
    }

    @Test
    @DisplayName("createForm() returns 'admin/manufacturers/form' with empty DTO")
    void createForm_returnsFormView() {
        String view = controller.createForm(model);

        assertThat(view).isEqualTo("admin/manufacturers/form");
        assertThat(model.asMap()).containsKey("form");
        assertThat(model.asMap().get("isEdit")).isEqualTo(false);
    }

    @Test
    @DisplayName("editForm() returns 'admin/manufacturers/form' with populated DTO and isEdit=true")
    void editForm_found_returnsFormView() {
        given(manufacturerService.getById(1L)).willReturn(samsung);

        String view = controller.editForm(1L, model);

        assertThat(view).isEqualTo("admin/manufacturers/form");
        assertThat(model.asMap().get("isEdit")).isEqualTo(true);
        assertThat(model.asMap()).containsKey("mfrId");
    }

    @Test
    @DisplayName("editForm() propagates exception when manufacturer not found")
    void editForm_notFound_throwsException() {
        given(manufacturerService.getById(99L)).willThrow(new ManufacturerNotFoundException(99L));

        assertThatThrownBy(() -> controller.editForm(99L, model))
                .isInstanceOf(ManufacturerNotFoundException.class);
    }

    @Test
    @DisplayName("delete() calls service and redirects to /admin/manufacturers")
    void delete_success_redirectsToList() {
        var ra = new RedirectAttributesModelMap();

        String view = controller.delete(1L, ra);

        assertThat(view).isEqualTo("redirect:/admin/manufacturers");
        then(manufacturerService).should().delete(1L);
        assertThat(ra.getFlashAttributes()).containsKey("successMessage");
    }


    @Test
    @DisplayName("delete() propagates ManufacturerHasAppliancesException")
    void delete_hasAppliances_throwsException() {
        willThrow(new ManufacturerHasAppliancesException(1L)).given(manufacturerService).delete(1L);

        assertThatThrownBy(() -> controller.delete(1L, new RedirectAttributesModelMap()))
                .isInstanceOf(ManufacturerHasAppliancesException.class);
    }


    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("redirects to /admin/manufacturers on success")
        void create_valid_redirectsToList() {
            given(manufacturerService.create(dto)).willReturn(samsung);
            var ra = new RedirectAttributesModelMap();

            String view = controller.create(dto, noErrors, model, ra);

            assertThat(view).isEqualTo("redirect:/admin/manufacturers");
            then(manufacturerService).should().create(dto);
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("returns form view when binding errors exist")
        void create_bindingErrors_returnsFormView() {
            BindingResult errors = new BeanPropertyBindingResult(dto, "form");
            errors.rejectValue("name", "error", "required");

            String view = controller.create(dto, errors, model, new RedirectAttributesModelMap());

            assertThat(view).isEqualTo("admin/manufacturers/form");
            then(manufacturerService).should(never()).create(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("redirects to /admin/manufacturers on success")
        void update_valid_redirectsToList() {
            given(manufacturerService.update(1L, dto)).willReturn(samsung);
            var ra = new RedirectAttributesModelMap();

            String view = controller.update(1L, dto, noErrors, model, ra);

            assertThat(view).isEqualTo("redirect:/admin/manufacturers");
            then(manufacturerService).should().update(1L, dto);
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("returns form view when binding errors exist")
        void update_bindingErrors_returnsFormView() {
            BindingResult errors = new BeanPropertyBindingResult(dto, "form");
            errors.rejectValue("name", "error", "required");

            String view = controller.update(1L, dto, errors, model, new RedirectAttributesModelMap());

            assertThat(view).isEqualTo("admin/manufacturers/form");
            then(manufacturerService).should(never()).update(anyLong(), any());
        }
    }
}
