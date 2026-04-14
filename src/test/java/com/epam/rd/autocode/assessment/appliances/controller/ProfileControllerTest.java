package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileController Unit Tests")
class ProfileControllerTest {

    @Mock
    private ClientRepository clientRepository;
    @InjectMocks
    private ProfileController controller;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    private Model model;
    private Authentication clientAuth;
    private Client alice;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        clientAuth = new UsernamePasswordAuthenticationToken("alice@example.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        alice = new Client(1L, "Alice", "alice@example.com", "pw", null);
    }

    @Nested
    @DisplayName("profile()")
    class ProfileTests {

        @Test
        @DisplayName("returns 'profile/view' and adds client to model")
        void profile_found_returnsView() {
            given(clientRepository.findByEmail("alice@example.com")).willReturn(Optional.of(alice));

            String view = controller.profile(clientAuth, model);

            assertThat(view).isEqualTo("profile/view");
            assertThat(model.asMap().get("client")).isEqualTo(alice);
        }

        @Test
        @DisplayName("throws NoSuchElementException when client not in DB")
        void profile_notFound_throwsException() {
            given(clientRepository.findByEmail("alice@example.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> controller.profile(clientAuth, model))
                    .isInstanceOf(java.util.NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("updateCard()")
    class UpdateCardTests {

        @Test
        @DisplayName("saves trimmed card number and redirects to /profile")
        void updateCard_validCard_savesAndRedirects() {
            given(clientRepository.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(clientRepository.save(any())).willReturn(alice);
            var ra = new RedirectAttributesModelMap();

            String view = controller.updateCard("  CARD-1234  ", clientAuth, ra);

            assertThat(view).isEqualTo("redirect:/profile");
            then(clientRepository).should().save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getCard()).isEqualTo("CARD-1234");
            assertThat(ra.getFlashAttributes()).containsKey("successMessage");
        }

        @Test
        @DisplayName("sets card to null when blank string is submitted")
        void updateCard_blankCard_setsNullCard() {
            given(clientRepository.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(clientRepository.save(any())).willReturn(alice);

            controller.updateCard("   ", clientAuth, new RedirectAttributesModelMap());

            then(clientRepository).should().save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getCard()).isNull();
        }

        @Test
        @DisplayName("sets card to null when null is submitted")
        void updateCard_nullCard_setsNullCard() {
            given(clientRepository.findByEmail("alice@example.com")).willReturn(Optional.of(alice));
            given(clientRepository.save(any())).willReturn(alice);

            controller.updateCard(null, clientAuth, new RedirectAttributesModelMap());

            then(clientRepository).should().save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getCard()).isNull();
        }
    }
}
