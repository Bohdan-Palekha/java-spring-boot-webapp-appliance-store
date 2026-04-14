package com.epam.rd.autocode.assessment.appliances.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Boolean approved;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id")
    private Set<OrderRow> orderRowSet = new HashSet<>();

    public Orders() {
    }

    public Orders(Long id, Boolean approved, Client client, Employee employee, Set<OrderRow> orderRowSet) {
        this.id = id;
        this.approved = approved;
        this.client = client;
        this.employee = employee;
        this.orderRowSet = orderRowSet != null ? orderRowSet : new HashSet<>();
    }

    public void addOrderRow(OrderRow row) {
        this.orderRowSet.add(row);
    }

    public boolean isPending() {
        return approved == null;
    }

    public boolean isApproved() {
        return Boolean.TRUE.equals(approved);
    }

    public boolean isRejected() {
        return Boolean.FALSE.equals(approved);
    }

    public BigDecimal getTotal() {
        if (orderRowSet == null) return BigDecimal.ZERO;
        return orderRowSet.stream().map(OrderRow::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Set<OrderRow> getOrderRowSet() {
        return orderRowSet;
    }

    public void setOrderRowSet(Set<OrderRow> s) {
        this.orderRowSet = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Orders x)) return false;
        return id != null && id.equals(x.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
