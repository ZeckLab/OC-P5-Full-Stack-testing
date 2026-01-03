package com.openclassrooms.starterjwt.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Test")
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    // ***** DELETE *****
    @Test
    @DisplayName("delete() should call repository to delete session by id")
    public void delete_withId_shouldCallRepository() {
        // Act
        userService.delete(1L);
        
        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    // ***** FIND BY ID *****
    @Test
    @DisplayName("findById() with existing id should return user")
    public void findById_withExistingId_shouldReturnUser() {
        // Arrange
        User user = new User()
                .setId(1L)
                .setFirstName("Hugo")
                .setLastName("Lebolide");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Act
        User foundUser = userService.findById(1L);
        
        // Assert
        assertThat(foundUser).isEqualTo(user);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById() with unknown id should return null")
    public void findById_withUnknownId_shouldReturnNull() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act
        User foundUser = userService.findById(99L);
        
        // Assert
        assertThat(foundUser).isNull();
        verify(userRepository, times(1)).findById(99L);
    }
}
