package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.LoginAttempt;
import com.epam.rd.autocode.assessment.appliances.repository.LoginAttemptRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptService Unit Tests")
class LoginAttemptServiceTest {

    private static final String EMAIL = "user@test.com";
    @Mock
    private LoginAttemptRepository repo;
    @InjectMocks
    private LoginAttemptService service;
    @Captor
    private ArgumentCaptor<LoginAttempt> captor;

    @Nested
    @DisplayName("recordFailure()")
    class RecordFailureTests {

        @Test
        @DisplayName("creates new record with failCount=1 on first failure")
        void recordFailure_firstAttempt_createsRecordWithCount1() {
            given(repo.findByEmail(EMAIL)).willReturn(Optional.empty());
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.recordFailure(EMAIL);

            then(repo).should().save(captor.capture());
            assertThat(captor.getValue().getFailCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("increments existing failCount on subsequent failures")
        void recordFailure_existingRecord_incrementsCount() {
            LoginAttempt existing = new LoginAttempt();
            existing.setEmail(EMAIL);
            existing.setFailCount(2);
            given(repo.findByEmail(EMAIL)).willReturn(Optional.of(existing));
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.recordFailure(EMAIL);

            then(repo).should().save(captor.capture());
            assertThat(captor.getValue().getFailCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("locks account after 5 failed attempts")
        void recordFailure_fifthAttempt_locksAccount() {
            LoginAttempt existing = new LoginAttempt();
            existing.setEmail(EMAIL);
            existing.setFailCount(4); // One more will trigger lock
            given(repo.findByEmail(EMAIL)).willReturn(Optional.of(existing));
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.recordFailure(EMAIL);

            then(repo).should().save(captor.capture());
            LoginAttempt saved = captor.getValue();
            assertThat(saved.getFailCount()).isEqualTo(5);
            assertThat(saved.getLockedUntil()).isNotNull();
            assertThat(saved.getLockedUntil()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("resets counter if previous lock has expired")
        void recordFailure_expiredLock_resetsCounter() {
            LoginAttempt existing = new LoginAttempt();
            existing.setEmail(EMAIL);
            existing.setFailCount(5);
            existing.setLockedUntil(Instant.now().minus(30, ChronoUnit.MINUTES)); // expired
            given(repo.findByEmail(EMAIL)).willReturn(Optional.of(existing));
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.recordFailure(EMAIL);

            then(repo).should().save(captor.capture());
            assertThat(captor.getValue().getFailCount()).isEqualTo(1);
            assertThat(captor.getValue().getLockedUntil()).isNull();
        }
    }

    @Nested
    @DisplayName("recordSuccess()")
    class RecordSuccessTests {

        @Test
        @DisplayName("resets failCount and lockedUntil on successful login")
        void recordSuccess_existingRecord_resetsCountAndLock() {
            LoginAttempt existing = new LoginAttempt();
            existing.setEmail(EMAIL);
            existing.setFailCount(3);
            existing.setLockedUntil(Instant.now().plus(10, ChronoUnit.MINUTES));
            given(repo.findByEmail(EMAIL)).willReturn(Optional.of(existing));
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.recordSuccess(EMAIL);

            then(repo).should().save(captor.capture());
            assertThat(captor.getValue().getFailCount()).isEqualTo(0);
            assertThat(captor.getValue().getLockedUntil()).isNull();
        }

        @Test
        @DisplayName("does nothing when no record found for email")
        void recordSuccess_noRecord_doesNothing() {
            given(repo.findByEmail(EMAIL)).willReturn(Optional.empty());

            service.recordSuccess(EMAIL);

            then(repo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("isLocked()")
    class IsLockedTests {

        @Test
        @DisplayName("returns true when lockedUntil is in the future")
        void isLocked_activeLock_returnsTrue() {
            LoginAttempt attempt = new LoginAttempt();
            attempt.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
            given(repo.findByEmail(EMAIL)).willReturn(Optional.of(attempt));

            assertThat(service.isLocked(EMAIL)).isTrue();
        }

        @Test
        @DisplayName("returns false when lockedUntil is in the past")
        void isLocked_expiredLock_returnsFalse() {
            LoginAttempt attempt = new LoginAttempt();
            attempt.setLockedUntil(Instant.now().minus(5, ChronoUnit.MINUTES));
            given(repo.findByEmail(EMAIL)).willReturn(Optional.of(attempt));

            assertThat(service.isLocked(EMAIL)).isFalse();
        }

        @Test
        @DisplayName("returns false when no record exists")
        void isLocked_noRecord_returnsFalse() {
            given(repo.findByEmail(EMAIL)).willReturn(Optional.empty());
            assertThat(service.isLocked(EMAIL)).isFalse();
        }

        @Test
        @DisplayName("returns false when lockedUntil is null")
        void isLocked_nullLockedUntil_returnsFalse() {
            LoginAttempt attempt = new LoginAttempt();
            attempt.setLockedUntil(null);
            given(repo.findByEmail(EMAIL)).willReturn(Optional.of(attempt));

            assertThat(service.isLocked(EMAIL)).isFalse();
        }
    }
}
