package com.bookworm.backend.repository;

import com.bookworm.backend.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Used by AppUserDetailsService (called from JwtAuthFilter, outside any
    // request-bound transaction since open-in-view is false). Without eagerly
    // fetching role here, UserPrincipal.getAuthorities() throws
    // LazyInitializationException on user.getRole() once the session that
    // loaded the User is already closed - manifesting as every authenticated
    // request silently falling back to anonymous and getting denied with 403.
    @EntityGraph(attributePaths = "role")
    Optional<User> findWithRoleByEmail(String email);

    // Used by UserAdminService to block demoting/deactivating the last remaining ADMIN.
    long countByRole_RoleName(String roleName);
}
