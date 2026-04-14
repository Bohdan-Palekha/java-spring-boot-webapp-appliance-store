package com.epam.rd.autocode.assessment.appliances.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "appliances")
public class Appliance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Category category;

    @Column(length = 100)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "power_type", nullable = false, length = 15)
    private PowerType powerType;

    @Column(length = 500)
    private String characteristic;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer power;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public Appliance() {
    }

    public Appliance(Long id, String name, Category category, String model,
                     Manufacturer manufacturer, PowerType powerType,
                     String characteristic, String description,
                     Integer power, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.model = model;
        this.manufacturer = manufacturer;
        this.powerType = powerType;
        this.characteristic = characteristic;
        this.description = description;
        this.power = power;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public PowerType getPowerType() {
        return powerType;
    }

    public void setPowerType(PowerType powerType) {
        this.powerType = powerType;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appliance a)) return false;
        return id != null && id.equals(a.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
