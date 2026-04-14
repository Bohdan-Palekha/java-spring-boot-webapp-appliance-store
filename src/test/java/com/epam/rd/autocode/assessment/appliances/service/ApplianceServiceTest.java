package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ApplianceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.ApplianceServiceImpl;
import com.epam.rd.autocode.assessment.appliances.specification.ApplianceSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplianceService Unit Tests")
class ApplianceServiceTest {

    @Mock
    private ApplianceRepository applianceRepo;
    @Mock
    private ManufacturerRepository manufacturerRepo;
    @InjectMocks
    private ApplianceServiceImpl service;

    @Captor
    private ArgumentCaptor<Appliance> applianceCaptor;

    private Manufacturer samsung;
    private Appliance washer;
    private ApplianceFormDTO formDTO;

    @BeforeEach
    void setUp() {
        samsung = new Manufacturer(1L, "Samsung");
        washer = new Appliance(1L, "Samsung Washer", Category.BIG, "WW90T",
                samsung, PowerType.AC220, "9kg", "Front loader", 2000, new BigDecimal("799.99"));

        formDTO = new ApplianceFormDTO();
        formDTO.setName("New Washer");
        formDTO.setCategory(Category.BIG);
        formDTO.setModel("NW-100");
        formDTO.setManufacturerId(1L);
        formDTO.setPowerType(PowerType.AC220);
        formDTO.setPower(1800);
        formDTO.setPrice(new BigDecimal("699.99"));
    }

    @Test
    @DisplayName("countAll delegates to repository")
    void countAll_delegatesToRepo() {
        given(applianceRepo.countAll()).willReturn(42L);
        assertThat(service.countAll()).isEqualTo(42L);
        verify(applianceRepo).countAll();
    }

    @Test
    @DisplayName("countByCategory delegates to repository")
    void countByCategory_delegatesToRepo() {
        given(applianceRepo.countByCategory(Category.BIG)).willReturn(15L);
        assertThat(service.countByCategory(Category.BIG)).isEqualTo(15L);
        verify(applianceRepo).countByCategory(Category.BIG);
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("existing ID returns appliance entity")
        void existingId_returnsAppliance() {
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            Appliance result = service.getById(1L);
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Samsung Washer");
            verify(applianceRepo).findById(1L);
        }

        @Test
        @DisplayName("non-existent ID throws ApplianceNotFoundException")
        void nonExistentId_throwsNotFound() {
            given(applianceRepo.findById(999L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.getById(999L))
                    .isInstanceOf(ApplianceNotFoundException.class);
            verify(applianceRepo, never()).save(any());
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, 0L, 99999L, Long.MAX_VALUE})
        @DisplayName("various non-existent IDs throw ApplianceNotFoundException")
        void variousNonExistentIds_throwNotFound(long id) {
            given(applianceRepo.findById(id)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.getById(id))
                    .isInstanceOf(ApplianceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("returns paginated appliances")
        void returnsPaginatedAppliances() {
            Pageable pageable = PageRequest.of(0, 12);
            Page<Appliance> page = new PageImpl<>(List.of(washer), pageable, 1);
            given(applianceRepo.findAll(pageable)).willReturn(page);

            Page<Appliance> result = service.getAll(pageable);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Samsung Washer");
        }

        @Test
        @DisplayName("empty repository returns empty page")
        void emptyRepo_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 12);
            given(applianceRepo.findAll(pageable)).willReturn(Page.empty());
            Page<Appliance> result = service.getAll(pageable);
            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getFeatured()")
    class GetFeaturedTests {

        @Test
        @DisplayName("returns up to requested limit")
        void returnsUpToLimit() {
            Page<Appliance> page = new PageImpl<>(List.of(washer));
            given(applianceRepo.findAll(any(PageRequest.class))).willReturn(page);
            List<Appliance> result = service.getFeatured(8);
            assertThat(result).hasSize(1);
            verify(applianceRepo).findAll(any(PageRequest.class));
        }

        @Test
        @DisplayName("clamps limit to 1-20 range")
        void limitsAreClampedTo1to20() {
            given(applianceRepo.findAll(any(PageRequest.class))).willReturn(Page.empty());
            service.getFeatured(0);  // should be clamped to 1
            service.getFeatured(50); // should be clamped to 20
            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            verify(applianceRepo, times(2)).findAll(captor.capture());
            List<PageRequest> calls = captor.getAllValues();
            assertThat(calls.get(0).getPageSize()).isEqualTo(1);
            assertThat(calls.get(1).getPageSize()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("search()")
    class SearchTests {

        @Test
        @DisplayName("null criteria returns all via specification")
        void allNullCriteria_usesSpecification() {
            Pageable pageable = PageRequest.of(0, 12);
            given(applianceRepo.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(washer)));

            Page<Appliance> result = service.search(null, null, null, null, null, null, pageable);
            assertThat(result.getContent()).hasSize(1);
            verify(applianceRepo).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("with keyword — delegates with non-null specification")
        void withKeyword_delegatesToSpecification() {
            Pageable pageable = PageRequest.of(0, 12);
            given(applianceRepo.findAll(any(Specification.class), eq(pageable))).willReturn(Page.empty());
            service.search("samsung", null, null, null, null, null, pageable);
            verify(applianceRepo).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("with category filter — passes to specification")
        void withCategoryFilter_usesSpecification() {
            Pageable pageable = PageRequest.of(0, 12);
            given(applianceRepo.findAll(any(Specification.class), eq(pageable))).willReturn(Page.empty());
            service.search(null, Category.BIG, null, null, null, null, pageable);
            verify(applianceRepo).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("ApplianceSpecification.hasCategory returns conjunction for null")
        void specification_nullCategory_isConjunction() {
            Specification<Appliance> spec = ApplianceSpecification.hasCategory(null);
            assertThat(spec).isNotNull();
        }

        @Test
        @DisplayName("ApplianceSpecification.hasKeyword returns conjunction for blank")
        void specification_blankKeyword_isConjunction() {
            Specification<Appliance> spec = ApplianceSpecification.hasKeyword("  ");
            assertThat(spec).isNotNull();
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("valid DTO creates and returns appliance")
        void validDTO_createsAppliance() {
            given(manufacturerRepo.findById(1L)).willReturn(Optional.of(samsung));
            given(applianceRepo.save(any(Appliance.class))).willReturn(washer);

            Appliance result = service.create(formDTO);
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Samsung Washer");

            verify(applianceRepo).save(applianceCaptor.capture());
            Appliance saved = applianceCaptor.getValue();
            assertThat(saved.getManufacturer()).isEqualTo(samsung);
            assertThat(saved.getId()).isNull(); // no over-posting — id must not be set
            assertThat(saved.getName()).isEqualTo("New Washer");
            assertThat(saved.getCategory()).isEqualTo(Category.BIG);
            assertThat(saved.getPrice()).isEqualByComparingTo("699.99");
        }

        @Test
        @DisplayName("invalid manufacturerId throws ManufacturerNotFoundException")
        void invalidManufacturerId_throwsException() {
            formDTO.setManufacturerId(99L);
            given(manufacturerRepo.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(formDTO))
                    .isInstanceOf(ManufacturerNotFoundException.class);
            verify(applianceRepo, never()).save(any());
        }

        @Test
        @DisplayName("create trims whitespace from name")
        void create_trimsName() {
            formDTO.setName("  Samsung Washer  ");
            given(manufacturerRepo.findById(1L)).willReturn(Optional.of(samsung));
            given(applianceRepo.save(any())).willReturn(washer);

            service.create(formDTO);
            verify(applianceRepo).save(applianceCaptor.capture());
            assertThat(applianceCaptor.getValue().getName()).isEqualTo("Samsung Washer");
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("valid update modifies and saves appliance")
        void validUpdate_modifiesAppliance() {
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            given(manufacturerRepo.findById(1L)).willReturn(Optional.of(samsung));
            given(applianceRepo.save(any(Appliance.class))).willReturn(washer);

            Appliance result = service.update(1L, formDTO);
            assertThat(result).isNotNull();
            verify(applianceRepo).findById(1L);
            verify(manufacturerRepo).findById(1L);
            verify(applianceRepo).save(any(Appliance.class));
        }

        @Test
        @DisplayName("non-existent appliance throws ApplianceNotFoundException")
        void nonExistentAppliance_throws() {
            given(applianceRepo.findById(999L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.update(999L, formDTO))
                    .isInstanceOf(ApplianceNotFoundException.class);
            verify(applianceRepo, never()).save(any());
        }

        @Test
        @DisplayName("invalid manufacturer throws ManufacturerNotFoundException")
        void invalidManufacturer_throws() {
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            formDTO.setManufacturerId(99L);
            given(manufacturerRepo.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(1L, formDTO))
                    .isInstanceOf(ManufacturerNotFoundException.class);
            verify(applianceRepo, never()).save(any());
        }

        @Test
        @DisplayName("update finds existing entity before saving")
        void update_findsExistingBeforeSaving() {
            InOrder order = inOrder(applianceRepo, manufacturerRepo);
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            given(manufacturerRepo.findById(1L)).willReturn(Optional.of(samsung));
            given(applianceRepo.save(any())).willReturn(washer);

            service.update(1L, formDTO);
            order.verify(applianceRepo).findById(1L);
            order.verify(applianceRepo).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("existing appliance is deleted")
        void existingAppliance_isDeleted() {
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            willDoNothing().given(applianceRepo).deleteById(1L);

            assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();
            verify(applianceRepo, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("non-existent appliance throws ApplianceNotFoundException")
        void nonExistentAppliance_throwsNotFound() {
            given(applianceRepo.findById(99L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.delete(99L))
                    .isInstanceOf(ApplianceNotFoundException.class);
            verify(applianceRepo, never()).deleteById(any());
        }

        @Test
        @DisplayName("findById called BEFORE deleteById")
        void delete_verifiesExistenceFirst() {
            InOrder inOrder = inOrder(applianceRepo);
            given(applianceRepo.findById(1L)).willReturn(Optional.of(washer));
            willDoNothing().given(applianceRepo).deleteById(1L);

            service.delete(1L);
            inOrder.verify(applianceRepo).findById(1L);
            inOrder.verify(applianceRepo).deleteById(1L);
        }
    }
}
