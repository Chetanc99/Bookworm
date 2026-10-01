package com.bookworm.backend.security.oauth2;

import com.bookworm.backend.entity.User;
import com.bookworm.backend.entity.UserRole;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * super.loadUser(...) actually talks to Google, so it can't be unit tested
 * without a live/mocked HTTP call. What CAN and should be verified here in
 * isolation is the find-or-create logic that CustomOidcUserService applies
 * once it has an email: reuse an existing account, or provision a new
 * CUSTOMER with a random (never-usable-for-password-login) password hash.
 */
@ExtendWith(MockitoExtension.class)
class CustomOidcUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomOidcUserService service;

    @Test
    void reusesExistingUserByEmail() {
        UserRole customerRole = UserRole.builder().roleId(1L).roleName("CUSTOMER").build();
        User existing = User.builder()
                .userId(42L)
                .email("someone@gmail.com")
                .fullName("Someone")
                .role(customerRole)
                .isActive(true)
                .build();

        when(userRepository.findByEmail("someone@gmail.com")).thenReturn(Optional.of(existing));

        User result = userRepository.findByEmail("someone@gmail.com")
                .orElseThrow();

        assertThat(result.getUserId()).isEqualTo(42L);
        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).findByRoleName(any());
    }

    @Test
    void provisionsNewCustomerWhenEmailUnknown() throws Exception {
        UserRole customerRole = UserRole.builder().roleId(1L).roleName("CUSTOMER").build();
        when(userRoleRepository.findByRoleName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded-random-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(99L);
            return u;
        });

        var method = CustomOidcUserService.class.getDeclaredMethod("provisionUser", String.class, String.class);
        method.setAccessible(true);
        User created = (User) method.invoke(service, "new.person@gmail.com", "New Person");

        assertThat(created.getUserId()).isEqualTo(99L);
        assertThat(created.getEmail()).isEqualTo("new.person@gmail.com");
        assertThat(created.getFullName()).isEqualTo("New Person");
        assertThat(created.getRole().getRoleName()).isEqualTo("CUSTOMER");
        assertThat(created.getPasswordHash()).isEqualTo("encoded-random-hash");
        assertThat(created.getIsActive()).isTrue();
        assertThat(created.getIsEmailVerified()).isTrue();
    }

    @Test
    void provisioningFallsBackToEmailWhenGoogleNameMissing() throws Exception {
        UserRole customerRole = UserRole.builder().roleId(1L).roleName("CUSTOMER").build();
        when(userRoleRepository.findByRoleName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded-random-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var method = CustomOidcUserService.class.getDeclaredMethod("provisionUser", String.class, String.class);
        method.setAccessible(true);
        User created = (User) method.invoke(service, "noname@gmail.com", null);

        assertThat(created.getFullName()).isEqualTo("noname@gmail.com");
    }
}
