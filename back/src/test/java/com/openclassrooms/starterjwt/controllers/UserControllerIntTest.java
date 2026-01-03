package com.openclassrooms.starterjwt.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.starterjwt.utils.TestAuthUtils;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("UserContoller Integration Test")
public class UserControllerIntTest {
    @Autowired
    private MockMvc mockMvc;
    
    private String token;
    
    @BeforeEach
    public void authenticate() throws Exception {
        token = TestAuthUtils.authenticate(mockMvc, "hugo@studio.com", "password");
    }

    // ***** FIND BY ID *****
    // User authenticated :   id 2 -> ('Hugo','Lebolide', false,'hugo@studio.com')

    @Test
    @DisplayName("GET /api/user/{id} without token should return 401 (Unauthorized)")
    public void findById_withoutToken_shouldReturn401() throws Exception {
        // No Authorization header → Spring Security blocks the request
        mockMvc.perform(get("/api/user/2"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/user/abc with invalid id should return 400 (Bad Request)")
    public void findById_withInvalidId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/user/abc")
            .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/user/{id} with a unknown id should return 404 (Not Found)")
    public void findById_withUnknownId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/user/666")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/user/{id} with a existing id should return 200 and user")
    void findById_withExistingId_shouldReturn200AndUser() throws Exception {
        // Ensures the controller returns the correct user information for Hugo (id = 2)
        mockMvc.perform(get("/api/user/2")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("firstName").value("Hugo"))
            .andExpect(jsonPath("lastName").value("Lebolide"));
    }

    // ***** DELETE / SAVE *****
    // User authenticated:   id 2 -> ('Hugo','Lebolide', false,'hugo@studio.com')

    @Test
    @DisplayName("DELETE /api/user/{id} without token should return 401 (Unauthorized)")
    public void save_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/user/2"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/user/abc with invalid id should return 400")
    public void save_withInvalidId_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/user/abc")
            .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/user/{id} with a unknown id should return 404 (Not Found)")
    public void save_withUnknownId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/user/666")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    // User :           id 2 -> ('Hugo','Lebolide', false,'hugo@studio.com') authenticated
    // Deleted user :   id 1 -> ('Admin', 'Admin', true,'yoga@studio.com') no authenticated
    // Hugo (id 2) is authenticated but tries to delete user 1 → forbidden
    @Test
    @DisplayName("DELETE /api/user/{id} with user not owner should return 401 (Unauthorized)")
    public void save_withUserNotOwner_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/user/1")
            .header("Authorization", token))
            .andExpect(status().isUnauthorized());
    }

    // User authenticated : id 2 -> ('Hugo','Lebolide', false,'hugo@studio.com') --> deleted user
    // User authenticated after to check : id 3 --> ('Bob','Léponge', false, 'bob@studio.com')
    @Test
    @DisplayName("DELETE /api/user/{id} with existing id should delete the user")
    public void save_withExistingId_shouldDeleteUser() throws Exception {
        // delete the user Hugo Lebolide with his own credentials
        mockMvc.perform(delete("/api/user/2")
            .header("Authorization", token))
            .andExpect(status().isOk());
        
        // Authenticate as Bob (id = 3) to verify that Hugo no longer exists
        String bobToken = TestAuthUtils.authenticate(mockMvc, "bob@studio.com", "password");
        
        // After deletion, Hugo should not be retrievable anymore
        mockMvc.perform(get("/api/user/2")
            .header("Authorization", bobToken))
            .andExpect(status().isNotFound());
        
    }
}

