package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT_CREDITS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_credit_id")
    private Long productCreditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_type_id", nullable = false)
    private CreditType creditType;

    // Deliberately a free-text name, not a Stakeholder FK - schema treats this as a lightweight
    // "who did this role" tag (e.g. Narrator: "Jane Doe") separate from formal Stakeholder records.
    @Column(name = "credit_value", nullable = false, length = 150)
    private String creditValue;
}
