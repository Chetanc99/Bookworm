package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER_ROLES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    // e.g. CUSTOMER, ADMIN - kept as free-text reference data rather than a Java enum
    // so new roles can be added via the DB without a redeploy, consistent with the
    // rest of the lookup tier (Categories, Languages, etc.).
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;
}
