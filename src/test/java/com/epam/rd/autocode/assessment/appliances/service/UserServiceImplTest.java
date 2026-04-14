package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ClientEditDTO;
import com.epam.rd.autocode.assessment.appliances.dto.RegisterFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.DuplicateEmailException;
import com.epam.rd.autocode.assessment.appliances.exception.UserNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.repository.*;
import com.epam.rd.autocode.assessment.appliances.service.impl.UserServiceImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private ClientRepository clientRepo;
    @Mock
    private EmployeeRepository employeeRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OrdersRepository ordersRepo;
    @Mock
    private RefreshTokenRepository refreshTokenRepo;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepo;
    @InjectMocks
    private UserServiceImpl service;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;
    @Captor
    private ArgumentCaptor<Employee> employeeCaptor;

    private Client alice;
    private Employee jane;
    private RegisterFormDTO registerDTO;

    @BeforeEach
    void setUp() {
        alice = new Client(1L, "Alice", "alice@example.com", "encoded_pw", null);
        jane = new Employee(2L, "Jane", "jane@store.com", "encoded_pw", "EMPLOYEE");

        registerDTO = new RegisterFormDTO();
        registerDTO.setName("Alice");
        registerDTO.setEmail("alice@example.com");
        registerDTO.setPassword("Password1!");
        registerDTO.setConfirmPassword("Password1!");
    }

    @Test
    @DisplayName("updateClient updates name and email")
    void updateClient_updatesFields() {
        ClientEditDTO editDTO = new ClientEditDTO();
        editDTO.setName("Alice Updated");
        editDTO.setEmail("alice.updated@example.com");

        given(clientRepo.findById(1L)).willReturn(Optional.of(alice));
        given(userRepo.existsByEmail("alice.updated@example.com")).willReturn(false);
        given(clientRepo.save(any())).willReturn(alice);

        service.updateClient(1L, editDTO);

        then(clientRepo).should().save(clientCaptor.capture());
        assertThat(clientCaptor.getValue().getName()).isEqualTo("Alice Updated");
    }

    @Test
    @DisplayName("existsByEmail delegates to userRepo")
    void existsByEmail_delegatesToRepo() {
        given(userRepo.existsByEmail("test@test.com")).willReturn(true);
        assertThat(service.existsByEmail("test@test.com")).isTrue();
    }

    @Test
    @DisplayName("countClients delegates to clientRepo")
    void countClients_delegatesToRepo() {
        given(clientRepo.count()).willReturn(5L);
        assertThat(service.countClients()).isEqualTo(5L);
    }

    @Test
    @DisplayName("countEmployees delegates to employeeRepo")
    void countEmployees_delegatesToRepo() {
        given(employeeRepo.count()).willReturn(3L);
        assertThat(service.countEmployees()).isEqualTo(3L);
    }

    @Nested
    @DisplayName("registerClient()")
    class RegisterClientTests {

        @Test
        @DisplayName("saves new client when email is not taken")
        void registerClient_uniqueEmail_savesClient() {
            given(userRepo.existsByEmail("alice@example.com")).willReturn(false);
            given(passwordEncoder.encode("Password1!")).willReturn("encoded_pw");
            given(clientRepo.save(any())).willReturn(alice);

            Client result = service.registerClient(registerDTO);

            assertThat(result).isEqualTo(alice);
            then(clientRepo).should().save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getEmail()).isEqualTo("alice@example.com");
        }

        @Test
        @DisplayName("throws DuplicateEmailException when email already exists")
        void registerClient_takenEmail_throwsException() {
            given(userRepo.existsByEmail("alice@example.com")).willReturn(true);

            assertThatThrownBy(() -> service.registerClient(registerDTO))
                    .isInstanceOf(DuplicateEmailException.class);
            then(clientRepo).should(never()).save(any());
        }

        @Test
        @DisplayName("normalizes email to lowercase before saving")
        void registerClient_normalizesEmail() {
            registerDTO.setEmail("ALICE@EXAMPLE.COM");
            given(userRepo.existsByEmail("ALICE@EXAMPLE.COM")).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(clientRepo.save(any())).willReturn(alice);

            service.registerClient(registerDTO);

            then(clientRepo).should().save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getEmail()).isEqualTo("alice@example.com");
        }
    }

    @Nested
    @DisplayName("processOAuth2PostLogin()")
    class OAuth2LoginTests {

        @Test
        @DisplayName("creates new client when email not registered")
        void oauth2Login_newUser_savesClient() {
            given(userRepo.existsByEmail("alice@example.com")).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("random_encoded");
            given(clientRepo.save(any())).willReturn(alice);

            Client result = service.processOAuth2PostLogin("Alice@Example.com", "Alice");

            assertThat(result).isEqualTo(alice);
            then(clientRepo).should().save(any());
        }

        @Test
        @DisplayName("returns existing client when email is already registered")
        void oauth2Login_existingUser_returnsExistingClient() {
            given(userRepo.existsByEmail("alice@example.com")).willReturn(true);
            given(userRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));

            Client result = service.processOAuth2PostLogin("alice@example.com", "Alice");

            assertThat(result).isEqualTo(alice);
            then(clientRepo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("returns user when found")
        void getById_found_returnsUser() {
            given(userRepo.findById(1L)).willReturn(Optional.of(alice));
            assertThat(service.getById(1L)).isEqualTo(alice);
        }

        @Test
        @DisplayName("throws UserNotFoundException when not found")
        void getById_notFound_throwsException() {
            given(userRepo.findById(99L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.getById(99L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByEmail()")
    class GetByEmailTests {

        @Test
        @DisplayName("returns user when found by email")
        void getByEmail_found_returnsUser() {
            given(userRepo.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            assertThat(service.getByEmail("alice@example.com")).isEqualTo(alice);
        }

        @Test
        @DisplayName("throws UserNotFoundException when email not registered")
        void getByEmail_notFound_throwsException() {
            given(userRepo.findByEmail("nobody@test.com")).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.getByEmail("nobody@test.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createEmployee()")
    class CreateEmployeeTests {

        @BeforeEach
        void prepareDto() {
            registerDTO.setEmail("jane@store.com");
            registerDTO.setName("Jane");
        }

        @Test
        @DisplayName("creates and saves employee with correct department")
        void createEmployee_uniqueEmail_savesEmployee() {
            given(userRepo.existsByEmail("jane@store.com")).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(employeeRepo.save(any())).willReturn(jane);

            Employee result = service.createEmployee(registerDTO, "EMPLOYEE");

            assertThat(result).isEqualTo(jane);
            then(employeeRepo).should().save(employeeCaptor.capture());
            assertThat(employeeCaptor.getValue().getDepartment()).isEqualTo("EMPLOYEE");
        }

        @Test
        @DisplayName("throws DuplicateEmailException when email already taken")
        void createEmployee_takenEmail_throwsException() {
            given(userRepo.existsByEmail("jane@store.com")).willReturn(true);

            assertThatThrownBy(() -> service.createEmployee(registerDTO, "EMPLOYEE"))
                    .isInstanceOf(DuplicateEmailException.class);
            then(employeeRepo).should(never()).save(any());
        }
    }
}
