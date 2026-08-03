package com.bookworm.backend.config;

import com.bookworm.backend.entity.LibraryPackage;
import com.bookworm.backend.repository.LibraryPackageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LibraryPackageBootstrapConfigTest {

    private final LibraryPackageRepository repository = mock(LibraryPackageRepository.class);
    private final LibraryPackageBootstrapConfig config = new LibraryPackageBootstrapConfig();

    @Test
    void seedLibraryPackages_noneExist_createsAllThreeTiers() throws Exception {
        when(repository.existsByPackageNameIgnoreCase(anyString())).thenReturn(false);

        CommandLineRunner runner = config.seedLibraryPackages(repository);
        runner.run();

        verify(repository, times(3)).save(any(LibraryPackage.class));
        verify(repository).save(argThat(p -> "Weekly".equals(p.getPackageName())
                && p.getDurationDays() == 7 && p.getMaxConcurrentBorrows() == 3));
        verify(repository).save(argThat(p -> "Monthly".equals(p.getPackageName())
                && p.getDurationDays() == 30 && p.getMaxConcurrentBorrows() == 10));
        verify(repository).save(argThat(p -> "Quarterly".equals(p.getPackageName())
                && p.getDurationDays() == 90 && p.getMaxConcurrentBorrows() == 30));
    }

    @Test
    void seedLibraryPackages_allAlreadyExist_createsNothing() throws Exception {
        when(repository.existsByPackageNameIgnoreCase(anyString())).thenReturn(true);

        CommandLineRunner runner = config.seedLibraryPackages(repository);
        runner.run();

        verify(repository, never()).save(any());
    }
}
