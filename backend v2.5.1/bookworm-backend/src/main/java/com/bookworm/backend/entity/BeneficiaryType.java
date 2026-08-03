package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Admin-managed preset (e.g. "Author", "Publisher", "Narrator", "Translator",
 * "Editor", "Platform", "Instructor", "Course Creator") carrying a
 * defaultRoyaltyPercentage. Purely a convenience default for
 * ProductBeneficiaryServiceImpl.addSplit() - the actual percentage always
 * lives on ProductBeneficiary (per product, per beneficiary), never here, so
 * changing a preset never silently changes an existing split. New types can
 * be added by admins with no code changes (no enum, no switch statement).
 * Soft-delete via isActive, same reasoning as Beneficiary/Category - existing
 * Beneficiary rows may still reference a deactivated type.
 */
@Entity
@Table(name = "BENEFICIARY_TYPES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "beneficiary_type_id")
    private Long beneficiaryTypeId;

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // Default only - ProductBeneficiary.royaltyPercentage is what actually gets
    // paid out; admins may override per product when business rules allow.
    @Column(name = "default_royalty_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultRoyaltyPercentage;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
