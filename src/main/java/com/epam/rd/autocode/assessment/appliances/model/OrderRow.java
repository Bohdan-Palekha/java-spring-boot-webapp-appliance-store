package com.epam.rd.autocode.assessment.appliances.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_rows")
public class OrderRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appliance_id", nullable = false)
    private Appliance appliance;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Long number;

    public OrderRow() {
    }

    public OrderRow(Long id, Appliance appliance, BigDecimal amount, Long number) {
        this.id = id;
        this.appliance = appliance;
        this.amount = amount;
        this.number = number;
    }

    public static OrderRow of(Appliance appliance, Long quantity) {
        BigDecimal total = appliance.getPrice().multiply(BigDecimal.valueOf(quantity));
        return new OrderRow(null, appliance, total, quantity);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appliance getAppliance() {
        return appliance;
    }

    public void setAppliance(Appliance appliance) {
        this.appliance = appliance;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderRow r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
