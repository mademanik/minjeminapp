package com.minjemin.product.service;

import com.minjemin.product.model.Item;
import com.minjemin.product.model.Rental;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RentalSpecification {
    public static Specification<Rental> filterRentals(String borrowerId, String name, String status) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            //Filter based on borrowerId
            predicates.add(criteriaBuilder.equal(root.get("borrowerId"), borrowerId));

            // Filter based on Item Name
            if (name != null && !name.isBlank()) {
                // Melakukan JOIN ke entitas Item
                Join<Rental, Item> itemJoin = root.join("item");
                String normalizedName = "%" + name.toLowerCase() + "%";
                // WHERE LOWER(item.name) LIKE '%input%'
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(itemJoin.get("name")),
                        normalizedName
                ));
            }

            // Filter based on Status
            if (status != null && !status.isBlank()) {
                String normalizedStatus = "%" + status.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("status").as(String.class)),
                        normalizedStatus
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
