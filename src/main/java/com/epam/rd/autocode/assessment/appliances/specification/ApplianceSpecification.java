package com.epam.rd.autocode.assessment.appliances.specification;

import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ApplianceSpecification {
    private ApplianceSpecification() {
    }

    public static Specification<Appliance> hasCategory(Category category) {
        return (r, q, cb) -> category == null ? cb.conjunction() : cb.equal(r.get("category"), category);
    }

    public static Specification<Appliance> hasKeyword(String keyword) {
        return (r, q, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String p = "%" + keyword.toLowerCase().trim() + "%";
            return cb.or(
                    cb.like(cb.lower(r.get("name")), p),
                    cb.like(cb.lower(r.get("description")), p),
                    cb.like(cb.lower(r.get("characteristic")), p)
            );
        };
    }

    public static Specification<Appliance> priceBetween(BigDecimal min, BigDecimal max) {
        return (r, q, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min == null) return cb.lessThanOrEqualTo(r.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(r.get("price"), min);
            return cb.between(r.get("price"), min, max);
        };
    }

    public static Specification<Appliance> hasPowerType(PowerType pt) {
        return (r, q, cb) -> pt == null ? cb.conjunction() : cb.equal(r.get("powerType"), pt);
    }

    public static Specification<Appliance> hasManufacturer(Long mfrId) {
        return (r, q, cb) -> mfrId == null ? cb.conjunction() : cb.equal(r.get("manufacturer").get("id"), mfrId);
    }
}
