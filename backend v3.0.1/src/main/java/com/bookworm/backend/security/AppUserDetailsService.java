package com.bookworm.backend.security;

import com.bookworm.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // findWithRoleByEmail (not findByEmail) - eagerly fetches role so
        // UserPrincipal.getAuthorities() doesn't hit a closed-session lazy
        // proxy when called later, outside this method's scope (see
        // UserRepository for the LazyInitializationException this avoids).
        return userRepository.findWithRoleByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + email));
    }
}
