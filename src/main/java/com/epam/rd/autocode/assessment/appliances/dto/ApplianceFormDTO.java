package com.epam.rd.autocode.assessment.appliances.dto;

import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ApplianceFormDTO {
    @NotBlank(message = "{appliance.name.notblank}")
    @Size(min = 2, max = 200, message = "{appliance.name.size}")
    private String name;

    @NotNull(message = "{appliance.category.notnull}")
    private Category category;

    @Size(max = 100)
    private String model;

    @NotNull(message = "{appliance.manufacturer.notnull}")
    @Positive(message = "{appliance.manufacturer.positive}")
    private Long manufacturerId;

    @NotNull(message = "{appliance.powerType.notnull}")
    private PowerType powerType;

    @Size(max = 500, message = "{appliance.characteristic.size}")
    private String characteristic;

    @Size(max = 2000, message = "{appliance.description.size}")
    private String description;

    @NotNull(message = "{appliance.power.notnull}")
    @Positive(message = "{appliance.power.positive}")
    @Max(value = 100000, message = "{appliance.power.max}")
    private Integer power;

    @NotNull(message = "{appliance.price.notnull}")
    @DecimalMin(value = "0.01", message = "{appliance.price.min}")
    @Digits(integer = 8, fraction = 2, message = "{appliance.price.digits}")
    private BigDecimal price;
}
