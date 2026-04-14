package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.controller.admin.AdminUserController;
import com.epam.rd.autocode.assessment.appliances.dto.ClientEditDTO;
import com.epam.rd.autocode.assessment.appliances.dto.RegisterFormDTO;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.service.UserService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserController Unit Tests")
class AdminUserControllerTest {

    @Mock
    private UserService userService;
    @InjectMocks
    private AdminUserController controller;

    private Model model;
    private Client alice;
    private Employee jane;
    private RegisterFormDTO registerDTO;
    private BindingResult noErrors;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        alice = new Client(1L, "Alice", "alice@example.com", "pw", null);
        jane = new Employee(2L, "Jane", "jane@store.com", "pw", "General");

        registerDTO = new RegisterFormDTO();
        registerDTO.setName("Alice");
        registerDTO.setEmail("alice@example.com");
        registerDTO.setPassword("Password1!");
        registerDTO.setConfirmPassword("Password1!");

        noErrors = new BeanPropertyBindingResult(registerDTO, "form");
    }

    @Test
    @DisplayName("list() returns 'admin/users/list' with user search results")
    void list_returnsView() {
        given(userService.searchUsers(any(), any()))
                .willReturn(new PageImpl<>(List.of(alice)));

        String view = controller.list(null, 0, 20, model);

        assertThat(view).isEqualTo("admin/users/list");
        assertThat(model.asMap()).containsKey("users");
    }

    @Test
    @DisplayName("list() passes keyword to service search")
    void list_withKeyword_passesKeywordToService() {
        given(userService.searchUsers(eq("alice"), any()))
                .willReturn(new PageImpl<>(List.of(alice)));

        controller.list("alice", 0, 20, model);

        then(userService).should().searchUsers(eq("alice"), any());
    }


    @Test
    @DisplayName("clients() returns 'admin/users/clients' with client list")
    void clients_returnsView() {
        given(userService.getAllClients(any())).willReturn(new PageImpl<>(List.of(alice)));

        String view = controller.clients(0, model);

        assertThat(view).isEqualTo("admin/users/clients");
        assertThat(model.asMap()).containsKeys("clients", "totalPages", "currentPage");
    }


    @Test
    @DisplayName("createClientForm() returns 'admin/users/client-form' with empty DTO")
    void createClientForm_returnsFormView() {
        String view = controller.createClientForm(model);

        assertThat(view).isEqualTo("admin/users/client-form");
        assertThat(model.asMap()).containsKey("form");
    }

    @Test
    @DisplayName("editClientForm() returns 'admin/users/client-edit-form' with pre-populated DTO")
    void editClientForm_found_returnsEditForm() {
        given(userService.getById(1L)).willReturn(alice);

        String view = controller.editClientForm(1L, model);

        assertThat(view).isEqualTo("admin/users/client-edit-form");
        assertThat(model.asMap()).containsKeys("form", "clientId");
    }

    @Test
    @DisplayName("deleteClient() calls service.deleteUser and redirects to /admin/users/clients")
    void deleteClient_success_redirectsToClients() {
        var ra = new RedirectAttributesModelMap();

        String view = controller.deleteClient(1L, ra);

        assertThat(view).isEqualTo("redirect:/admin/users/clients");
        then(userService).should().deleteUser(1L);
        assertThat(ra.getFlashAttributes()).containsKey("successMessage");
    }

    @Test
    @DisplayName("employees() returns 'admin/users/employees' with employee list")
    void employees_returnsView() {
        given(userService.getAllEmployees(any())).willReturn(new PageImpl<>(List.of(jane)));

        String view = controller.employees(0, model);

        assertThat(view).isEqualTo("admin/users/employees");
        assertThat(model.asMap()).containsKeys("employees", "totalPages", "currentPage");
    }

    @Test
    @DisplayName("createEmployeeForm() returns 'admin/users/employee-form'")
    void createEmployeeForm_returnsFormView() {
        String view = controller.createEmployeeForm(model);

        assertThat(view).isEqualTo("admin/users/employee-form");
        assertThat(model.asMap()).containsKey("form");
    }

    @Test
    @DisplayName("deleteEmployee() calls service.deleteEmployee and redirects to /admin/users/employees")
    void deleteEmployee_success_redirectsToEmployees() {
        var ra = new RedirectAttributesModelMap();

        String view = controller.deleteEmployee(2L, ra);

        assertThat(view).isEqualTo("redirect:/admin/users/employees");
        then(userService).should().deleteEmployee(2L);
        assertThat(ra.getFlashAttributes()).containsKey("successMessage");
    }

    @Test
    @DisplayName("updateDepartment() calls service and redirects to /admin/users/employees")
    void updateDepartment_success_redirectsToEmployees() {
        var ra = new RedirectAttributesModelMap();

        String view = controller.updateDepartment(2L, "HR", ra);

        assertThat(view).isEqualTo("redirect:/admin/users/employees");
        then(userService).should().updateDepartment(2L, "HR");
        assertThat(ra.getFlashAttributes()).containsKey("successMessage");
    }

    @Nested
    @DisplayName("createClient()")
    class CreateClientTests {

        @Test
        @DisplayName("registers client and redirects to /admin/users/clients")
        void createClient_valid_redirectsToClients() {
            given(userService.registerClient(registerDTO)).willReturn(alice);
            var ra = new RedirectAttributesModelMap();

            String view = controller.createClient(registerDTO, noErrors, model, ra);

            assertThat(view).isEqualTo("redirect:/admin/users/clients");
            then(userService).should().registerClient(registerDTO);
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("returns client form when passwords don't match")
        void createClient_passwordMismatch_returnsForm() {
            registerDTO.setConfirmPassword("OtherPass!");
            BindingResult errors = new BeanPropertyBindingResult(registerDTO, "form");

            String view = controller.createClient(registerDTO, errors, model, new RedirectAttributesModelMap());

            assertThat(view).isEqualTo("admin/users/client-form");
            then(userService).should(never()).registerClient(any());
        }

        @Test
        @DisplayName("returns client form when binding errors exist")
        void createClient_bindingErrors_returnsForm() {
            BindingResult errors = new BeanPropertyBindingResult(registerDTO, "form");
            errors.rejectValue("name", "error", "required");

            String view = controller.createClient(registerDTO, errors, model, new RedirectAttributesModelMap());

            assertThat(view).isEqualTo("admin/users/client-form");
            then(userService).should(never()).registerClient(any());
        }
    }

    @Nested
    @DisplayName("updateClient()")
    class UpdateClientTests {

        private ClientEditDTO editDTO;

        @BeforeEach
        void setup() {
            editDTO = new ClientEditDTO();
            editDTO.setName("Alice Updated");
            editDTO.setEmail("alice@example.com");
        }

        @Test
        @DisplayName("updates client and redirects to /admin/users/clients")
        void updateClient_valid_redirectsToClients() {
            BindingResult result = new BeanPropertyBindingResult(editDTO, "form");
            given(userService.updateClient(1L, editDTO)).willReturn(alice);
            var ra = new RedirectAttributesModelMap();

            String view = controller.updateClient(1L, editDTO, result, model, ra);

            assertThat(view).isEqualTo("redirect:/admin/users/clients");
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("returns edit form when binding errors exist")
        void updateClient_bindingErrors_returnsEditForm() {
            BindingResult errors = new BeanPropertyBindingResult(editDTO, "form");
            errors.rejectValue("name", "error", "required");

            String view = controller.updateClient(1L, editDTO, errors, model,
                    new RedirectAttributesModelMap());

            assertThat(view).isEqualTo("admin/users/client-edit-form");
            then(userService).should(never()).updateClient(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("createEmployee()")
    class CreateEmployeeTests {

        @Test
        @DisplayName("creates employee and redirects to /admin/users/employees")
        void createEmployee_valid_redirectsToEmployees() {
            given(userService.createEmployee(registerDTO, "Engineering")).willReturn(jane);
            var ra = new RedirectAttributesModelMap();

            String view = controller.createEmployee(registerDTO, "Engineering", noErrors, model, ra);

            assertThat(view).isEqualTo("redirect:/admin/users/employees");
            then(userService).should().createEmployee(registerDTO, "Engineering");
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("returns employee form when passwords don't match")
        void createEmployee_passwordMismatch_returnsForm() {
            registerDTO.setConfirmPassword("OtherPass!");
            BindingResult errors = new BeanPropertyBindingResult(registerDTO, "form");

            String view = controller.createEmployee(registerDTO, "Engineering", errors, model,
                    new RedirectAttributesModelMap());

            assertThat(view).isEqualTo("admin/users/employee-form");
            then(userService).should(never()).createEmployee(any(), any());
        }
    }
}
