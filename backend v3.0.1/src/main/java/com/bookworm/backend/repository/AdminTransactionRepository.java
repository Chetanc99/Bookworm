package com.bookworm.backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Native SQL report over PURCHASE_TRANSACTIONS + RENT_TRANSACTIONS, joined
 * to USERS for email/name. Plain JpaRepository can't express a UNION ALL
 * across two unrelated entities, so this goes straight to the EntityManager
 * (mirrors the project's "native SQL reports" backlog item). ORDER BY is
 * fixed to created_at DESC - Pageable.getSort() is intentionally not honored
 * here, only paging (limit/offset).
 */
@Repository
@RequiredArgsConstructor
public class AdminTransactionRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    private static final String UNION_SQL = """
            SELECT 'PURCHASE' AS transaction_type, pt.purchase_transaction_id AS id, pt.user_id AS user_id,
                   u.email AS user_email, u.full_name AS user_full_name,
                   pt.total_amount AS total_amount, pt.status AS status, pt.created_at AS created_at
            FROM PURCHASE_TRANSACTIONS pt
            JOIN USERS u ON u.user_id = pt.user_id
            UNION ALL
            SELECT 'RENT' AS transaction_type, rt.rent_transaction_id AS id, rt.user_id AS user_id,
                   u.email AS user_email, u.full_name AS user_full_name,
                   rt.total_amount AS total_amount, rt.status AS status, rt.created_at AS created_at
            FROM RENT_TRANSACTIONS rt
            JOIN USERS u ON u.user_id = rt.user_id
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """;

    private static final String COUNT_SQL = """
            SELECT (SELECT COUNT(*) FROM PURCHASE_TRANSACTIONS) + (SELECT COUNT(*) FROM RENT_TRANSACTIONS)
            """;

    // PurchaseTransaction.Status = COMPLETED only (no other value is ever set).
    // RentTransaction.Status = ACTIVE/EXPIRED/CANCELLED - CANCELLED is excluded
    // (no charge actually stuck), ACTIVE and EXPIRED both represent money
    // already collected at rent-checkout time.
    private static final String REVENUE_SQL = """
            SELECT
                (SELECT COALESCE(SUM(total_amount), 0) FROM PURCHASE_TRANSACTIONS WHERE status = 'COMPLETED')
                + (SELECT COALESCE(SUM(total_amount), 0) FROM RENT_TRANSACTIONS WHERE status IN ('ACTIVE', 'EXPIRED'))
                AS total_revenue,
                (SELECT COUNT(*) FROM PURCHASE_TRANSACTIONS WHERE status = 'COMPLETED') AS purchase_count,
                (SELECT COUNT(*) FROM RENT_TRANSACTIONS WHERE status IN ('ACTIVE', 'EXPIRED')) AS rent_count
            """;

    @SuppressWarnings("unchecked")
    public List<Tuple> findPage(int limit, int offset) {
        return entityManager.createNativeQuery(UNION_SQL, Tuple.class)
                .setParameter("limit", limit)
                .setParameter("offset", offset)
                .getResultList();
    }

    public long countAll() {
        return ((Number) entityManager.createNativeQuery(COUNT_SQL).getSingleResult()).longValue();
    }

    public Tuple getRevenueSummary() {
        return (Tuple) entityManager.createNativeQuery(REVENUE_SQL, Tuple.class).getSingleResult();
    }
}
