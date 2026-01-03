package com.openclassrooms.starterjwt.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import java.util.Optional;

@SpringBootTest
@DisplayName("AuthController Unit Tests")
public class AuthControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthController authController;
    
    // ***** AUTHENTICATE USER *****
    @Test
    @DisplayName("authenticateUser() with a valid loginRequest should return 200 and a JwtResponse")
    public void authenticateUser_withValidLoginRequest_shouldReturn200AndJwtResponse() {
        // Arrange: prepare a valid login request
        String email = "test@test.com", password = "password";
        String firstName = "Hugo", lastName = "Lebolide";
        String token = "jwt";
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        // Mock authentication success
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        
        // Mock JWT generation
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(token);
        
        // Mock authenticated user details
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn(email);
        when(userDetails.getFirstName()).thenReturn(firstName);
        when(userDetails.getLastName()).thenReturn(lastName);
        
        // Mock user lookup (admin = true)
        User user = User.builder()
            .id(1L)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .password(password)
            .admin(true)
            .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(JwtResponse.class);
        
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertThat(jwtResponse).isNotNull();
        assertThat(jwtResponse.getToken()).isEqualTo(token);
        assertThat(jwtResponse.getId()).isEqualTo(1L);
        assertThat(jwtResponse.getUsername()).isEqualTo(email);
        assertThat(jwtResponse.getFirstName()).isEqualTo(firstName);
        assertThat(jwtResponse.getLastName()).isEqualTo(lastName);
        assertThat(jwtResponse.getAdmin()).isTrue();
        
        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("authenticateUser() with invalid loginRequest should return 401")
    public void authenticateUser_withInvalidLoginRequest_shouldReturn401() {
        // Arrange
        String email = "test@test.com", password = "wrongpassword";
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        // Mock authentication failure
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));
        
        // Act + Assert
        assertThatThrownBy(() -> authController.authenticateUser(loginRequest))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("Bad credentials");
        
        // Verify no further processing occurs
        verify(authenticationManager, times(1)).authenticate(any());
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("authenticateUser() with user not found should return 200 and admin false")
    public void authenticateUser_withUserNotFound_shouldReturn200AndAdminFalse() {
        // Arrange
        String email = "test@test.com", password = "password";
        String firstName = "Hugo", lastName = "Lebolide";
        String token = "jwt";
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        // Mock authentication success
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(token);
        
        // Mock authenticated user details
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn(email);
        when(userDetails.getFirstName()).thenReturn(firstName);
        when(userDetails.getLastName()).thenReturn(lastName);
        
        // Mock user not found → admin defaults to false
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(JwtResponse.class);
        
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertThat(jwtResponse).isNotNull();
        assertThat(jwtResponse.getToken()).isEqualTo(token);
        assertThat(jwtResponse.getAdmin()).isFalse();
        
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
        verify(userRepository, times(1)).findByEmail(email);
    }

    // ***** REGISTER USER *****

    @Test
    @DisplayName("registerUser() with valid SignupRequest should return 200 and MessageResponse")
    public void registerUser_withValidSignupRequest_shouldReturn200AndMessageResponse() {
        // Arrange
        String email = "test@test.com", password = "password";
        String firstName = "Hugo", lastName = "Lebolide";
        
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName(firstName);
        signupRequest.setLastName(lastName);
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded-password");
        
        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(MessageResponse.class);
        
        MessageResponse message = (MessageResponse) response.getBody();
        assertThat(message.getMessage()).isEqualTo("User registered successfully!");
        
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser() with existing email should return 400 and a Messageresponse")
    public void registerUser_withExistingEmail_shouldReturn400AndMessageResponse() {
        // Arrange
        String email = "test@test.com", password = "password";
        String firstName = "Hugo", lastName = "Lebolide";
        
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName(firstName);
        signupRequest.setLastName(lastName);
        
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(MessageResponse.class);
        
        MessageResponse message = (MessageResponse) response.getBody();
        assertThat(message.getMessage()).isEqualTo("Error: Email is already taken!");
        
        verify(userRepository, times(1)).existsByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }
}