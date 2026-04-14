package com.epam.rd.autocode.assessment.appliances.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderFormDTO {
    @NotNull(message = "{order.appliance.notnull}")
    @Positive(message = "{order.appliance.positive}")
    private Long applianceId;
    @NotNull(message = "{order.quantity.notnull}")
    @Min(value = 1, message = "{order.quantity.min}")
    @Max(value = 100, message = "{order.quantity.max}")
    private Long quantity;
}
