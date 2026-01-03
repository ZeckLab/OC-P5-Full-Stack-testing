package com.openclassrooms.starterjwt.security.jwt;

import static org.assertj.core.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@DisplayName("JwtUtils Test")
public class JwtUtilsTest {
    private static final String SECRET_KEY = "testSecretKey123456789";
    private static final int EXPIRATION_MS = 3600000;
    
    private JwtUtils jwtUtils;
    
    @BeforeEach
    public void setUp() {
        jwtUtils = new JwtUtils();
        
        // Inject secret and expiration since JwtUtils normally reads them from application properties
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);
    }

    // ***** GENERATE JWT TOKEN *****
    @Test
    @DisplayName("generateJwtToken() with valid authentication should return token")
    public void generateJwtToken_withValidAuthentication_shouldReturnToken() {
        // Arrange
        String username = "test@test.com";
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
            .id(2L)
            .username(username)
            .password("password")
            .build();
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null);
        
        // Act
        String createdToken = jwtUtils.generateJwtToken(authentication);
        
        // Assert
        assertThat(createdToken).isNotNull();
        Claims claims = Jwts.parser()
            .setSigningKey(SECRET_KEY)
            .parseClaimsJws(createdToken)
            .getBody();
        
        assertThat(claims.getSubject()).isEqualTo(username);
    }

    // ***** GET USERNAME FROM JWT TOKEN *****

    @Test
    @DisplayName("getUserNameFromJwtToken() should return correct username")
    public void getUserNameFromJwtToken_shouldReturnCorrectUsername() {
        // Arrange
        String username = "test@test.com";
        String token = Jwts.builder()
            .setSubject(username)
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();
        
        // Act
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);
        
        // Assert
        assertThat(extractedUsername).isEqualTo(username);
    }

    // ***** VALIDATE JWT TOKEN *****
    @Test
    @DisplayName("validateJwtToken() with valid token should return true")
    public void validateJwtToken_withValidToken_shouldReturnTrue() {
        // Arrange
        String token = Jwts.builder()
            .setSubject("test@test.com")
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();
        
        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);
        
        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateJwtToken() with expired token should return false")
    public void validateJwtToken_withExpiredToken_shouldReturnFalse() {
        // Arrange
        String token = Jwts.builder()
            .setSubject("test@test.com")
            .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
            .setExpiration(new Date(System.currentTimeMillis() - 5000))
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();
        
        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken() with malformed token should return false")
    public void validateJwtToken_withMalformedToken_shouldReturnFalse() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken("not-a-token");
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken() with invalid signature should return false")
    public void validateJwtToken_withInvalidSignature_shouldReturnFalse() {
        // Ararnge
        String token = Jwts.builder()
            .setSubject("test@test.com")
            .signWith(SignatureAlgorithm.HS512, "wrongSecretKey")
            .compact();
        
        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken() with unsupported token should return false")
    public void validateJwtToken_withUnsupportedToken_shouldReturnFalse() {
        // Arrange
        String token = Jwts.builder()
            .setSubject("test@test.com")
            .compact();
        
        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);
        
        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken() with empty token should return false")
    public void validateJwtToken_withEmptyToken_shouldReturnFalse() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken("");
        
        // Assert
        assertThat(isValid).isFalse();
    }
}