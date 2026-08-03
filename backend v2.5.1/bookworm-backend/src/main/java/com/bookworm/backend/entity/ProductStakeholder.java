package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT_STAKEHOLDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStakeholder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_stakeholder_id")
    private Long productStakeholderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stakeholder_id", nullable = false)
    private Stakeholder stakeholder;

    // Role at the time of this credit (e.g. "Author") - can differ from Stakeholder.type,
    // since the same person can be an Author on one product and an Editor on another.
    @Column(name = "role", length = 50)
    private String role;
}
