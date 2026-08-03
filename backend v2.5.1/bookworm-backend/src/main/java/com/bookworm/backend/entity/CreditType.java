package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CREDIT_TYPES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_type_id")
    private Long creditTypeId;

    @Column(name = "credit_type_name", nullable = false, unique = true, length = 50)
    private String creditTypeName;
}
