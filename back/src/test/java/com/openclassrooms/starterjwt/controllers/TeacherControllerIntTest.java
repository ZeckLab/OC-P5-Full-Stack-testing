package com.openclassrooms.starterjwt.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.openclassrooms.starterjwt.utils.TestAuthUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TeacherContoller Integration Test")
public class TeacherControllerIntTest {
    @Autowired
    private MockMvc mockMvc;
    
    private String token;
    
    @BeforeEach
    public void authenticate() throws Exception {
        token = TestAuthUtils.authenticate(mockMvc, "yoga@studio.com", "test!1234");
    }

    // ***** FIND BY ID *****
    // Donnée attendue :
    // Teacher :   id 1 -> Margot DELAHAYE

    @Test
    @DisplayName("GET /api/teacher/{id} without token should return 401 (Unauthorized)")
    public void findById_withoutToken_shouldReturn401() throws Exception {
        // Missing Authorization header → request rejected by Spring Security
        mockMvc.perform(get("/api/teacher/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/teacher/abc with invalid id should return 400 (Bad Request)")
    public void findById_withInvalidId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/teacher/abc")
            .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/teacher/{id} with a unknown id should return 404 (Not Found)")
    public void findById_withUnknownId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/teacher/666")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/teacher/{id} with a existing id should return 200 and teacher")
    public void findById_withExistingId_shouldReturn200AndTeacher() throws Exception {
        mockMvc.perform(get("/api/teacher/1")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("firstName").value("Margot"))
            .andExpect(jsonPath("lastName").value("DELAHAYE"));
    }

    // ***** FIND ALL *****
    // Données attendues :
    // Teachers :   id 1 -> Margot DELAHAYE
    //              id 2 -> Hélène THIERCELIN

    @Test
    @DisplayName("GET /api/teacher should return list of teachers")
    public void findAll_shouldReturnListOfTeachers() throws Exception {
        mockMvc.perform(get("/api/teacher")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].firstName").value("Margot"))
            .andExpect(jsonPath("$[0].lastName").value("DELAHAYE"))
            .andExpect(jsonPath("$[1].firstName").value("Hélène"))
            .andExpect(jsonPath("$[1].lastName").value("THIERCELIN"));
    }

}
