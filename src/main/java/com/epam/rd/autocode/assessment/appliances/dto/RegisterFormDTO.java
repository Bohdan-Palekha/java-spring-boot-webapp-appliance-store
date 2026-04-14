package com.epam.rd.autocode.assessment.appliances.dto;

import com.epam.rd.autocode.assessment.appliances.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterFormDTO {
    @NotBlank(message = "{user.name.notblank}")
    @Size(min = 2, max = 100, message = "{user.name.size}")
    private String name;
    @NotBlank(message = "{user.email.notblank}")
    @Email(message = "{user.email.invalid}")
    @UniqueEmail
    private String email;
    @NotBlank(message = "{user.password.notblank}")
    @Size(min = 8, max = 72, message = "{user.password.size}")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,72}$", message = "{user.password.weak}")
    private String password;
    @NotBlank(message = "{user.confirmPassword.notblank}")
    private String confirmPassword;
}
