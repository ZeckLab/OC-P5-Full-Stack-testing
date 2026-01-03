package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Test")
public class UserControllerTest {
    @Mock
    private UserService userService;
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserController userController;

    // ***** FIND BY ID *****

    @Test
    @DisplayName("findById() with existing id should return 200 and UserDto")
    public void findById_withExistingId_shouldReturn200AndUserDto() {
        // Arrange
        User user = new User()
            .setId(1L)
            .setFirstName("Hugo")
            .setLastName("Lebolide")
            .setEmail("test@test.com");
        
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("Hugo");
        userDto.setLastName("Lebolide");
        userDto.setEmail("test@test.com");
        
        when(userService.findById(1L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);
        
        // Act
        ResponseEntity<?> response = userController.findById("1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userDto);
        verify(userService, times(1)).findById(1L);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @DisplayName("findById() with unknown id should return 404 (Not Found)")
    public void findById_withUnknownId_shouldReturn404() {
        // Arrange
        when(userService.findById(666L)).thenReturn(null);
        
        // Act
        ResponseEntity<?> response = userController.findById("666");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService, times(1)).findById(666L);
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    @DisplayName("findById() with invalid id should return 400 (Bad request)")
    public void findById_withInvalidId_shouldReturn400() {
        // Act
        ResponseEntity<?> response = userController.findById("abc");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).findById(anyLong());
    }

    // ***** DELETE *****

    @Test
    @DisplayName("delete() with existing id should return 200")
    public void delete_withExistingId_shouldReturn200() {
        // Arrange
        User user = new User()
            .setId(1L)
            .setFirstName("Hugo")
            .setLastName("Lebolide")
            .setEmail("test@test.com");
        
        when(userService.findById(1L)).thenReturn(user);
        
        // Mock authenticated user in SecurityContext (required for delete endpoint)
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("test@test.com");
        
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
        
        // Act
        ResponseEntity<?> response = userController.save("1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService, times(1)).findById(1L);
        verify(userService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("delete() with unknown id should return 404 (Not Found)")
    public void delete_withUnknownId_shouldReturn404() {
        // Arrange
        when(userService.findById(666L)).thenReturn(null);
        
        // Act
        ResponseEntity<?> response = userController.save("666");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService, times(1)).findById(666L);
        verify(userService, never()).delete(1L);
    }

    @Test
    @DisplayName("delete() with invalid id should return 400 (Bad Request)")
    public void delete_withInvalidId_shouldReturn400() {
        // Act
        ResponseEntity<?> response = userController.save("abc");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).delete(anyLong());
    }

    @Test
    @DisplayName("delete() with user not owner should return 401 (Unauthorized)")
    public void delete_withUserIsNotOwner_shouldReturn401() {
        // Arrange
        User user = new User()
            .setId(1L)
            .setFirstName("Hugo")
            .setLastName("Lebolide")
            .setEmail("test@test.com");
        
        when(userService.findById(1L)).thenReturn(user);
        
        // Mock authenticated user in SecurityContext (required for delete endpoint)
        UserDetails principal = mock(UserDetails.class);
        // Authenticated user email does NOT match the user being deleted → should return 401
        when(principal.getUsername()).thenReturn("darkvador@test.com");
        
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
        
        // Act
        ResponseEntity<?> response = userController.save("1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(userService, times(1)).findById(1L);
        verify(userService, never()).delete(1L);
    }
}