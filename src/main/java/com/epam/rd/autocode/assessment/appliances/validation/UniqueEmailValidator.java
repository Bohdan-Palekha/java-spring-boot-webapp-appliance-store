package com.epam.rd.autocode.assessment.appliances.validation;

import com.epam.rd.autocode.assessment.appliances.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    private final UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext ctx) {
        if (email == null || email.isBlank()) return true;
        return !userRepository.existsByEmail(email.trim().toLowerCase());
    }
}
