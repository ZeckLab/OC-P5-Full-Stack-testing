package com.openclassrooms.starterjwt.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("AuthController Integration Test")
public class AuthControllerIntTest {
    @Autowired
    private MockMvc mockMvc;
    
    // ***** AUTHENTICATE USER *****
    
    @Test
    @DisplayName("POST /api/auth/login with invalid DTO should return 400 (Bad Request)")
    public void authenticateUser_withInvalidDto_shouldReturn400() throws Exception {
        // Missing password field → triggers DTO validation failure
        String loginDto = "{"
            + "\"email\":\"hugo@studio.com\""
            + "}";
        
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginDto))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login with wrong password should return 401 (Unauthorized)")
    public void authenticateUser_withWrongPassword_shouldReturn401() throws Exception {
        String loginDto = "{"
            + "\"email\":\"hugo@studio.com\","
            + "\"password\":\"wrongpassword\""
            + "}";
        
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginDto))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login with unknown email should return 401 (Unauthorized)")
    public void authenticateUser_withUnknownEmail_shouldReturn401() throws Exception {
        String loginDto = "{"
            + "\"email\":\"unknown@studio.com\","
            + "\"password\":\"password\""
            + "}";
        
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginDto))
            .andExpect(status().isUnauthorized());
    }

    // User : id 2 -> ('Hugo','Lebolide', false,'hugo@studio.com')
    @Test
    @DisplayName("POST /api/auth/login with valid credentials should return 200 and JwtResponse with token")
    public void authenticateUser_withValidCredentials_shouldReturn200AndJwtResponseWithToken() throws Exception {
        String loginDto = "{"
            + "\"email\":\"hugo@studio.com\","
            + "\"password\":\"password\""
            + "}";
        
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginDto))
            .andExpect(status().isOk())
            // Check the JwtResponse fields
            // token must exist and be non-empty
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.token").isNotEmpty())
            
            // type must be "Bearer"
            .andExpect(jsonPath("$.type").value("Bearer"))
            
            // id must exist and be numeric
            .andExpect(jsonPath("$.id").value(2))
            
            // username must exist and match the email
            .andExpect(jsonPath("$.username").value("hugo@studio.com"))
            
            // firstName and lastName must exist
            .andExpect(jsonPath("$.firstName").value("Hugo"))
            .andExpect(jsonPath("$.lastName").value("Lebolide"))
            
            // admin must exist and be boolean
            .andExpect(jsonPath("$.admin").value(false));
    }

    // ***** REGISTER USER *****

    @Test
    @DisplayName("POST /api/auth/register with invalid DTO should return 400 (Bad Request)")
    public void registerUser_withInvalidDto_shouldReturn400() throws Exception {
        // Missing required fields → triggers DTO validation failure
        String registerDto = "{"
            + "\"email\":\"hector@studio.com\""
            + "}";
        
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerDto))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register with existing email should return 400 (Bad Request)")
    public void registerUser_withExistingEmail_shouldReturn400() throws Exception {
        String registerDto = "{"
            + "\"email\":\"hugo@studio.com\","
            + "\"password\":\"password\","
            + "\"firstName\":\"Hector\","
            + "\"lastName\":\"Lecastor\""
            + "}";
        
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerDto))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Error: Email is already taken!"));
    }

    @Test
    @DisplayName("POST /api/auth/register with validDto should return 200 and create user")
    public void registerUser_withValidDto_shouldReturn200AndCreateUser() throws Exception {
        String registerDto = "{"
            + "\"email\":\"hector@studio.com\","
            + "\"password\":\"password\","
            + "\"firstName\":\"Hector\","
            + "\"lastName\":\"Lecastor\""
            + "}";
        
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerDto))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User registered successfully!"));
        
        // After registration, the user should be able to authenticate successfully
        String loginDto = "{"
            + "\"email\":\"hector@studio.com\","
            + "\"password\":\"password\""
            + "}";
        
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginDto))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.type").value("Bearer"))
            // New user receives ID 4 in the test database
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.username").value("hector@studio.com"))
            .andExpect(jsonPath("$.firstName").value("Hector"))
            .andExpect(jsonPath("$.lastName").value("Lecastor"))
            .andExpect(jsonPath("$.admin").value(false));
    }
}
