package com.openclassrooms.starterjwt.security.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;
    
    private User user;
    
    @BeforeEach
    public void prepareTestData() {
        user = new User()
                .setId(1L)
                .setFirstName("Hugo")
                .setLastName("Lebolide")
                .setEmail("test@test.com")
                .setPassword("password123")
                .setAdmin(false);
    }

    @Test
    @DisplayName("loadUserByUsername() with existing username should return UserDetails")
    public void loadUserByUsername_withExistingUsername_shouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        
        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");
        
        // Assert
        assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);
        assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
        
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        assertThat(userDetailsImpl.getId()).isEqualTo(1L);
        assertThat(userDetailsImpl.getFirstName()).isEqualTo("Hugo");
        assertThat(userDetailsImpl.getLastName()).isEqualTo("Lebolide");
        // Important: UserDetailsImpl does NOT expose the admin flag → always null
        assertThat(userDetailsImpl.getAdmin()).isNull();
        
        verify(userRepository, times(1)).findByEmail("test@test.com");
    }

    @Test
    @DisplayName("loadUserByUsername() with unknown username should throw UsernameNotFoundException")
    public void loadUserByUsername_withUnknownUsername_shouldThrowUsernameNotFoundException() {
        // Arrange
        String unknownUsername = "pouet@test.com";
        when(userRepository.findByEmail(unknownUsername)).thenReturn(Optional.empty());
        
        // Act + Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(unknownUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User Not Found with email: " + unknownUsername);
        
        verify(userRepository, times(1)).findByEmail(unknownUsername);
    }
}
