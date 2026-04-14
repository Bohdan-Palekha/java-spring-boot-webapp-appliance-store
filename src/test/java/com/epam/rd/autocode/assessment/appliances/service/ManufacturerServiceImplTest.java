package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerFormDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerHasAppliancesException;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerNameTakenException;
import com.epam.rd.autocode.assessment.appliances.exception.ManufacturerNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.ManufacturerServiceImpl;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManufacturerService Unit Tests")
class ManufacturerServiceImplTest {

    @Mock
    private ManufacturerRepository repo;
    @InjectMocks
    private ManufacturerServiceImpl service;

    @Captor
    private ArgumentCaptor<Manufacturer> captor;

    private Manufacturer samsung;
    private ManufacturerFormDTO dto;

    @BeforeEach
    void setUp() {
        samsung = new Manufacturer(1L, "Samsung");
        dto = new ManufacturerFormDTO();
        dto.setName("Samsung");
    }

    @Test
    @DisplayName("getAllManufacturers delegates to repo and returns results")
    void getAllManufacturers_delegatesAndReturns() {
        given(repo.findAllByOrderByNameAsc()).willReturn(List.of(samsung));

        List<Manufacturer> result = service.getAllManufacturers();

        assertThat(result).containsExactly(samsung);
        then(repo).should().findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("getPaginated delegates to repo.findAll(pageable)")
    void getPaginated_delegatesToRepo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Manufacturer> page = new PageImpl<>(List.of(samsung));
        given(repo.findAll(pageable)).willReturn(page);

        Page<Manufacturer> result = service.getPaginated(pageable);

        assertThat(result.getContent()).containsExactly(samsung);
        then(repo).should().findAll(pageable);
    }

    @Test
    @DisplayName("existsByName delegates to repo")
    void existsByName_delegatesToRepo() {
        given(repo.existsByName("Samsung")).willReturn(true);
        assertThat(service.existsByName("Samsung")).isTrue();
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("returns manufacturer when found")
        void getById_found_returnsManufacturer() {
            given(repo.findById(1L)).willReturn(Optional.of(samsung));
            assertThat(service.getById(1L)).isEqualTo(samsung);
        }

        @Test
        @DisplayName("throws ManufacturerNotFoundException when not found")
        void getById_notFound_throwsException() {
            given(repo.findById(99L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.getById(99L))
                    .isInstanceOf(ManufacturerNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("creates and saves manufacturer when name is unique")
        void create_uniqueName_savesManufacturer() {
            given(repo.existsByName("Samsung")).willReturn(false);
            given(repo.save(any())).willReturn(samsung);

            Manufacturer result = service.create(dto);

            assertThat(result).isEqualTo(samsung);
            then(repo).should().save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("Samsung");
        }

        @Test
        @DisplayName("throws ManufacturerNameTakenException when name already exists")
        void create_duplicateName_throwsException() {
            given(repo.existsByName("Samsung")).willReturn(true);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(ManufacturerNameTakenException.class);
            then(repo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("updates manufacturer name when valid")
        void update_validInput_updatesName() {
            dto.setName("Samsung Electronics");
            Manufacturer updated = new Manufacturer(1L, "Samsung Electronics");

            given(repo.findById(1L)).willReturn(Optional.of(samsung));
            given(repo.existsByNameAndIdNot("Samsung Electronics", 1L)).willReturn(false);
            given(repo.save(any())).willReturn(updated);

            Manufacturer result = service.update(1L, dto);

            assertThat(result.getName()).isEqualTo("Samsung Electronics");
        }

        @Test
        @DisplayName("throws ManufacturerNotFoundException when ID does not exist")
        void update_notFound_throwsException() {
            given(repo.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(99L, dto))
                    .isInstanceOf(ManufacturerNotFoundException.class);
            then(repo).should(never()).save(any());
        }

        @Test
        @DisplayName("throws ManufacturerNameTakenException when name is taken by another")
        void update_nameTakenByOther_throwsException() {
            given(repo.findById(1L)).willReturn(Optional.of(samsung));
            given(repo.existsByNameAndIdNot("Samsung", 1L)).willReturn(true);

            assertThatThrownBy(() -> service.update(1L, dto))
                    .isInstanceOf(ManufacturerNameTakenException.class);
            then(repo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("deletes manufacturer when found and has no appliances")
        void delete_noAppliances_deletesManufacturer() {
            given(repo.findById(1L)).willReturn(Optional.of(samsung));
            given(repo.existsAppliancesForManufacturer(1L)).willReturn(false);

            service.delete(1L);

            then(repo).should().delete(samsung);
        }

        @Test
        @DisplayName("throws ManufacturerNotFoundException when ID not found")
        void delete_notFound_throwsException() {
            given(repo.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(99L))
                    .isInstanceOf(ManufacturerNotFoundException.class);
            then(repo).should(never()).delete(any());
        }

        @Test
        @DisplayName("throws ManufacturerHasAppliancesException when appliances exist")
        void delete_hasAppliances_throwsException() {
            given(repo.findById(1L)).willReturn(Optional.of(samsung));
            given(repo.existsAppliancesForManufacturer(1L)).willReturn(true);

            assertThatThrownBy(() -> service.delete(1L))
                    .isInstanceOf(ManufacturerHasAppliancesException.class);
            then(repo).should(never()).delete(any());
        }
    }
}
