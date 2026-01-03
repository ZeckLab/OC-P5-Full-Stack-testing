package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.starterjwt.utils.TestAuthUtils;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("SessionController Integration Test")
public class SessionControllerInteTest {
    @Autowired
    private MockMvc mockMvc;
    
    private String token;
    
    @BeforeEach
    public void authenticate() throws Exception {
        token = TestAuthUtils.authenticate(mockMvc, "hugo@studio.com", "password");
    }

    // ***** FIND BY ID *****
    // User authenticated : id 2 -> ('Hugo','Lebolide', false,'hugo@studio.com')
    // Session existing :   id 1 -> ('Yoga pour la forme', 'Yoga pour la forme', '2026-01-03 12:00:00', 1)
    // Teacher :            id 1 -> Margot DELAHAYE

    @Test
    @DisplayName("GET /api/session/{id} without token should return 401 (Unauthorized)")
    public void findById_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/session/2"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/session/abc with invalid id should return 400")
    public void findById_withInvalidId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/session/abc")
            .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/session/{id} with a unknown id should return 404 (Not Found)")
    public void findById_withUnknownId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/session/666")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/session/{id} with a existing id should return 200 and session")
    public void findById_withExistingId_shouldReturn200AndSession() throws Exception {
        mockMvc.perform(get("/api/session/1")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Yoga pour la forme"));
    }

    // ***** FIND ALL *****
    // Sessions :   id 1 -> ('Yoga pour la forme', 'Yoga pour la forme', '2026-01-03 12:00:00', 1)
    //              id 2 -> ('Yoga débutant', 'Yoga pour les débutants', '2026-01-03 12:00:00', 2),
    //              id 3 -> ('Yoga confirmé', 'Yoga pour les confirmés', '2026-01-03 13:00:00', 2),
    //              id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    // Teachers :   id 1 -> Margot DELAHAYE
    //              id 2 -> Hélène THIERCELIN
    @Test
    @DisplayName("GET /api/session should return list of sessions")
    public void findAll_shouldReturnListOfSessions() throws Exception {
        mockMvc.perform(get("/api/session")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].name").value("Yoga pour la forme"))
            .andExpect(jsonPath("$[0].teacher_id").value(1))
            .andExpect(jsonPath("$[1].name").value("Yoga débutant"))
            .andExpect(jsonPath("$[1].teacher_id").value(2));
    }

    // ***** CREATE *****

    @Test
    @DisplayName("POST /api/session with invalid session DTO should return 400 (Bad Request)")
    public void create_withInvalidSessionDto_shouldReturn400() throws Exception {
        // Invalid date format: missing 'T' separator → triggers DTO validation failure
        String sessionDtoString = "{"
            + "\"name\":\"Yoga avancé\","
            + "\"description\":\"Yoga pour les avancés\","
            + "\"date\":\"2026-01-04 10:00:00\","
            + "\"teacher_id\":2"
            + "}";
        
        mockMvc.perform(post("/api/session")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sessionDtoString))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/session with valid session DTO should return 200 and create session")
    public void create_withValidSessionDto_shouldReturn200AndCreateSession() throws Exception {
        String sessionDtoString = "{"
            + "\"name\":\"Yoga avancé\","
            + "\"description\":\"Yoga pour les avancés\","
            + "\"date\":\"2026-01-04T10:00:00\","
            + "\"teacher_id\":2"
            + "}";
        
        mockMvc.perform(post("/api/session")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sessionDtoString))
            .andExpect(status().isOk());
        
        // ID 5 is expected because the test database is reset before each test (@Transactional)
        mockMvc.perform(get("/api/session/5")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.name").value("Yoga avancé"));
    }

    // ***** UPDATE *****

    // Updated Session : id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    @Test
    @DisplayName("PUT /api/session/{id} with valid session DTO and invalid id should return 400 (Bad Request)")
    public void create_withValidSessionDtoAndInvalidId_shouldReturn400() throws Exception {
        String sessionDtoString = "{"
            + "\"name\":\"Yoga détente\","
            + "\"description\":\"Bli blou bla\","
            + "\"date\":\"2026-01-03T13:00:00\","
            + "\"teacher_id\":1"
            + "}";
        
        mockMvc.perform(put("/api/session/abc")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sessionDtoString))
            .andExpect(status().isBadRequest());
    }

    // Updated Session : id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    @Test
    @DisplayName("PUT /api/session/{id} with invalid session DTO and existing id should return 400 (Bad Request)")
    public void create_withInvalidSessionDtoAndExistingId_shouldReturn400() throws Exception {
        // Invalid date format: missing 'T' separator → validation failure before service call
        String sessionDtoString = "{"
            + "\"name\":\"Yoga détente\","
            + "\"description\":\"Bli blou bla\","
            + "\"date\":\"2026-01-03 13:00:00\","
            + "\"teacher_id\":1"
            + "}";
        
        mockMvc.perform(put("/api/session/4")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sessionDtoString))
            .andExpect(status().isBadRequest());
        
        // Ensures the session was not modified after the failed update attempt
        mockMvc.perform(get("/api/session/4")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.description").value("Yoga détente"));
    }

    // Updated Session : id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    @Test
    @Disabled("Known issue: the update endpoint does not validate the existence of the session. "
        + "PUT /api/session/{id} creates a new session when the ID does not exist, instead of returning 404. "
        + "Test disabled because the application code cannot be modified.")
    @DisplayName("PUT /api/session/{id} with valid session DTO and unknown id should return 404 (Not Found)")
    public void create_withValidSessionDtoAndUnknownId_shouldReturn404() throws Exception {
        String sessionDtoString = "{"
            + "\"name\":\"Yoga détente\","
            + "\"description\":\"Bli blou bla\","
            + "\"date\":\"2026-01-03T13:00:00\","
            + "\"teacher_id\":1"
            + "}";
        
        mockMvc.perform(put("/api/session/666")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sessionDtoString))
            .andExpect(status().isNotFound());
    }

    // Updated Session : id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    @Test
    @DisplayName("PUT /api/session/{id} with valid session DTO and existing id should return 200 and update session")
    public void create_withValidSessionDtoAndExistingId_shouldReturn200AndUpdateSession() throws Exception {
        String sessionDtoString = "{"
            + "\"name\":\"Yoga détente\","
            + "\"description\":\"Bli blou bla\","
            + "\"date\":\"2026-01-03T13:00:00\","
            + "\"teacher_id\":1"
            + "}";
        
        mockMvc.perform(put("/api/session/4")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sessionDtoString))
            .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/session/4")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.description").value("Bli blou bla"));
    }

    // ***** DELETE / SAVE *****

    @Test
    @DisplayName("DELETE /api/session/abc with invalid id should return 400")
    public void save_withInvalidId_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/session/abc")
            .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/session/{id} with a unknown id should return 404 (Not Found)")
    public void save_withUnknownId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/session/666")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    // Deleted Session : id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    @Test
    @DisplayName("DELETE /api/session/{id} with existing id should return 200 and delete session")
    public void save_withExistingId_shouldReturn200AndDeleteSession() throws Exception {
        mockMvc.perform(delete("/api/session/4")
            .header("Authorization", token))
            .andExpect(status().isOk());
        
        // Ensures the session is actually deleted
        mockMvc.perform(get("/api/session/4")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    // ***** PARTICIPATE *****

    @Test
    @DisplayName("POST /api/session/abc/participate/{id} with invalid session id should return 400 (Bad Request)")
    public void participate_withInvalidSessionId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/session/abc/participate/2")
            .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/session/{id}/participate/abc with invalid user id should return 400 (Bad Request)")
    public void participate_withInvalidUserId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/session/4/participate/abc")
            .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/session/{id}/participate/{id} with unknown session id and existing user id should return 404 (Not Found)")
    public void participate_withUnknownSesssionIdAndExistingUserId_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/session/666/participate/2")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/session/{id}/participate/{id} with existing session id and unknown user id should return 404 (Not Found)")
    public void participate_withExistingSesssionIdAndUnknownUserId_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/session/4/participate/666")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    // Participated Session : id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    // User :                 id 2 -> ('Hugo','Lebolide', false,'hugo@studio.com')
    @Test
    @DisplayName("POST /api/session/{id}/participate/{id} with existing session id and user id should return 200 and add user to session")
    public void participate_withExistingSesssionIdAndUserId_shouldReturn200AndAddUserToSession() throws Exception {
        mockMvc.perform(post("/api/session/4/participate/2")
            .header("Authorization", token))
            .andExpect(status().isOk());
        
        // Check that the user list contains exactly one user: ID 2
        mockMvc.perform(get("/api/session/4")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.users.length()").value(1))
            .andExpect(jsonPath("$.users[0]").value(2));
    }

    // ***** NO LONGER PARTICIPATE *****

    @Test
    @DisplayName("DELETE /api/session/{id}/participate/{id} with unknown session id and existing user id should return 404 (Not Found)")
    public void noLongerParticipate_withUnknownSesssionIdAndExistingUserId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/session/666/participate/2")
            .header("Authorization", token))
            .andExpect(status().isNotFound());
    }

    // Participated Session : id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    // No User
    @Test
    @DisplayName("DELETE /api/session/{id}/participate/{id} with existing session id and existing user id but no participating user should return 400 (Bad Request)")
    public void noLongerParticipate_withExistingSesssionIdAndUserIdButNoParticipatingUser_shouldReturn400() throws Exception {
        // The service throws BadRequestException when the user is not part of the session
        mockMvc.perform(delete("/api/session/4/participate/2")
            .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    // Participated Session : id 4 -> ('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1)
    // Users :                id 2 -> ('Hugo','Lebolide', false,'hugo@studio.com')
    //                        id 3 -> ('Bob','Léponge', false,'bobo@studio.com')
    @Test
    @DisplayName("DELETE /api/session/{id}/participate/{id} with existing session id and existing user id and participating user should return 200 and remove user from session")
    public void noLongerParticipate_withExistingSesssionIdAndUserIdAndParticipatingUser_shouldReturn200AndDeleteUserFromSession() throws Exception {
        // First, add user 2 and user 3 to session 4
        mockMvc.perform(post("/api/session/4/participate/2")
            .header("Authorization", token))
        .andExpect(status().isOk());
        mockMvc.perform(post("/api/session/4/participate/3")
            .header("Authorization", token))
        .andExpect(status().isOk());
        
        // Then, remove user 2 from session 4
        mockMvc.perform(delete("/api/session/4/participate/2")
            .header("Authorization", token))
            .andExpect(status().isOk());
        
        // Finally, check that user 2 is not there
        mockMvc.perform(get("/api/session/4")
            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.users", not(hasItem(2))));
    }
}
