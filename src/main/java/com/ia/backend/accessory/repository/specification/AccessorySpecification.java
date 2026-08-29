package com.ia.backend.accessory.repository.specification;

import com.ia.backend.accessory.entity.Accessory;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class AccessorySpecification {

    public static Specification<Accessory> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Accessory> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("productCode")), pattern)
            );
        };
    }

    public static Specification<Accessory> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("price"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.lessThanOrEqualTo(root.get("price"), max);
        };
    }

    public static Specification<Accessory> inStock(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null) return null;
            return inStock
                    ? cb.greaterThan(root.get("stock"), 0)
                    : cb.equal(root.get("stock"), 0);
        };
    }
}