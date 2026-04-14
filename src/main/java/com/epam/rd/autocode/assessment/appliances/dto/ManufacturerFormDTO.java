package com.epam.rd.autocode.assessment.appliances.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ManufacturerFormDTO {
    @NotBlank(message = "{manufacturer.name.notblank}")
    @Size(min = 2, max = 100, message = "{manufacturer.name.size}")
    private String name;
}
