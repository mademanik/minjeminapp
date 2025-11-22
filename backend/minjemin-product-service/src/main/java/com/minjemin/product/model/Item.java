package com.minjemin.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private Double pricePerDay;

    @Column(nullable = false)
    private Integer stock = 1;

    private Boolean available = true;

    //get from keycloak
    @Column(nullable = false)
    private String ownerId;
}
