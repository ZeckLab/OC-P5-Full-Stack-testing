package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.openclassrooms.starterjwt.services.SessionService;
import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionController Unit Tests")
public class SessionControllerTest {
    @Mock
    private SessionService sessionService;
    
    @Mock
    private SessionMapper sessionMapper;
    
    @InjectMocks
    private SessionController sessionController;
    
    private Teacher teacher;
    private Session sessionWithoutId;
    private Session sessionWithId;
    private SessionDto sessionDtoWithoutId;
    private SessionDto sessionDtoWithId;
    
    @BeforeEach
    public void prepareTestData() {
        Date date = new Date();
        List<User> users = new ArrayList<>();
        
        teacher = Teacher.builder()
            .id(1L)
            .firstName("Maya")
            .lastName("Labeille")
            .build();
        
        sessionWithoutId = Session.builder()
            .id(null)
            .name("Yoga")
            .description("Cours de yoga pour débutants")
            .teacher(teacher)
            .date(date)
            .users(users)
            .build();
        
        sessionWithId = new Session()
            .setId(1L)
            .setName("Yoga")
            .setDescription("Cours de yoga pour débutants")
            .setTeacher(teacher)
            .setDate(date)
            .setUsers(users);
        
        sessionDtoWithoutId = new SessionDto();
        sessionDtoWithoutId.setId(null);
        sessionDtoWithoutId.setName("Yoga");
        sessionDtoWithoutId.setDescription("Cours de yoga pour débutants");
        sessionDtoWithoutId.setTeacher_id(1L);
        sessionDtoWithoutId.setDate(date);
        sessionDtoWithoutId.setUsers(new ArrayList<>());
        
        sessionDtoWithId = new SessionDto();
        sessionDtoWithId.setId(1L);
        sessionDtoWithId.setName("Yoga");
        sessionDtoWithId.setDescription("Cours de yoga pour débutants");
        sessionDtoWithId.setTeacher_id(1L);
        sessionDtoWithId.setDate(date);
        sessionDtoWithId.setUsers(new ArrayList<>());
    }
    
    // ***** FIND BY ID *****
    @Test
    @DisplayName("findById() with existing id should return 200 and SessionDto")
    public void findById_withExistingId_ShouldReturn200AndSessionDto() {
        // Arrange
        when(sessionService.getById(1L)).thenReturn(sessionWithId);
        when(sessionMapper.toDto(sessionWithId)).thenReturn(sessionDtoWithId);
        
        // Act
        ResponseEntity<?> response = sessionController.findById("1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sessionDtoWithId);
        verify(sessionService, times(1)).getById(1L);
        verify(sessionMapper, times(1)).toDto(sessionWithId);
    }
    
    @Test
    @DisplayName("findById() with unknown id should return 404 (Not Found)")
    public void findById_withUnknownId_ShouldReturn404() {
        // Arrange
        when(sessionService.getById(666L)).thenReturn(null);
        
        // Act
        ResponseEntity<?> response = sessionController.findById("666");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(sessionService, times(1)).getById(666L);
        verify(sessionMapper, never()).toDto(any(Session.class));
    }
    
    @Test
    @DisplayName("findById() with invalid id should return 400 (Bad Request)")
    public void findById_withInvalidId_ShouldReturn400() {
        // Act
        ResponseEntity<?> response = sessionController.findById("abc");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sessionService, never()).getById(anyLong());
    }
    
    // ***** FIND ALL *****
    @Test
    @DisplayName("findAll() should return 200 and list of SessionDto")
    public void findAll_ShouldReturn200AndListOfSessionDto() {
        // Arrange
        List<Session> sessions = new ArrayList<>(List.of(sessionWithId));
        List<SessionDto> sessionDtos = new ArrayList<>(List.of(sessionDtoWithId));
        
        when(sessionService.findAll()).thenReturn(sessions);
        when(sessionMapper.toDto(sessions)).thenReturn(sessionDtos);
        
        // Act
        ResponseEntity<?> response = sessionController.findAll();
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sessionDtos);
        verify(sessionService, times(1)).findAll();
        verify(sessionMapper, times(1)).toDto(sessions);
    }
    
    // ***** CREATE *****
    @Test
    @DisplayName("create() with valid SessionDto should return 200 and SessionDto")
    public void create_withValidSessionDto_ShouldReturn200AndSessionDto() {
        // Arrange
        when(sessionMapper.toEntity(sessionDtoWithoutId)).thenReturn(sessionWithoutId);
        when(sessionService.create(sessionWithoutId)).thenReturn(sessionWithId);
        when(sessionMapper.toDto(sessionWithId)).thenReturn(sessionDtoWithId);
        
        // Act
        ResponseEntity<?> response = sessionController.create(sessionDtoWithoutId);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sessionDtoWithId);
        verify(sessionMapper, times(1)).toEntity(sessionDtoWithoutId);
        verify(sessionService, times(1)).create(sessionWithoutId);
        verify(sessionMapper, times(1)).toDto(sessionWithId);
    }

    // ***** UPDATE *****
    @Test
    @DisplayName("update() with valid id and SessionDto should return 200 and SessionDto")
    public void update_withValidIdAndSessionDto_ShouldReturn200AndSessionDto() {
        // Arrange
        // Session to update does not have an ID
        Session sessionToUpdate = sessionWithoutId;
        SessionDto sessionDtoToUpdate = sessionDtoWithoutId;
        // Expected session (after update) has the ID set
        Session updatedSession = sessionWithId;
        SessionDto updatedSessionDto = sessionDtoWithId;
        
        when(sessionMapper.toEntity(sessionDtoToUpdate)).thenReturn(sessionToUpdate);
        when(sessionService.update(1L, sessionToUpdate)).thenReturn(updatedSession);
        when(sessionMapper.toDto(updatedSession)).thenReturn(updatedSessionDto);
        
        // Act
        ResponseEntity<?> response = sessionController.update("1", sessionDtoToUpdate);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sessionDtoWithId);
        verify(sessionMapper, times(1)).toEntity(sessionDtoToUpdate);
        verify(sessionService, times(1)).update(1L, sessionToUpdate);
        verify(sessionMapper, times(1)).toDto(updatedSession);
    }
    
    @Test
    @DisplayName("update() with invalid id should return 400 (Bad Request)")
    public void update_withInvalidId_ShouldReturn400() {
        // Act
        ResponseEntity<?> response = sessionController.update("invalid", sessionDtoWithoutId);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sessionMapper, never()).toEntity(any(SessionDto.class));
        verify(sessionService, never()).update(anyLong(), any(Session.class));
    }

    // ***** DELETE *****
    @Test
    @DisplayName("delete() with existing id should return 200")
    public void delete_withExistingId_ShouldReturn200() {
        // Arrange
        when(sessionService.getById(1L)).thenReturn(sessionWithId);
        
        // Act
        ResponseEntity<?> response = sessionController.save("1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(sessionService, times(1)).getById(1L);
        verify(sessionService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("delete() with unknown id should return 404 (Not Found)")
    public void delete_withUnknownId_ShouldReturn404() {
        // Arrange
        when(sessionService.getById(666L)).thenReturn(null);
        
        // Act
        ResponseEntity<?> response = sessionController.save("666");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(sessionService, times(1)).getById(666L);
        verify(sessionService, never()).delete(anyLong());
    }

    @Test
    @DisplayName("delete() with invalid id should return 400 (Bad Request)")
    public void delete_withInvalidId_shouldReturn400() {
        // Act
        ResponseEntity<?> response = sessionController.save("invalid");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sessionService, never()).getById(anyLong());
    }

    // ***** PARTICIPATE *****

    @Test
    @DisplayName("participate() with existing session id and user id should return 200")
    public void participate_withExistingSessionIdAndUserId_ShouldReturn200() {
        // Act
        ResponseEntity<?> response = sessionController.participate("1", "1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(sessionService, times(1)).participate(1L, 1L);
    }

    @Test
    @DisplayName("participate() with invalid session id should return 400 (Bad Request)")
    public void participate_withInvalidSessionId_ShouldReturn400() {
        // Act
        ResponseEntity<?> response = sessionController.participate("abc", "1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sessionService, never()).participate(anyLong(), anyLong());
    }

    @Test
    @DisplayName("participate() with invalid user id should return 400 (Bad Request)")
    public void participate_withInvalidUserId_ShouldReturn400() {
        // Act
        ResponseEntity<?> response = sessionController.participate("1", "abc");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sessionService, never()).participate(anyLong(), anyLong());
    }

    // ***** NO LONGER PARTICIPATE *****

    @Test
    @DisplayName("noLongerParticipate with existing session id and user id should return 200")
    public void noLongerParticipate_withExistingSessionIdAndUserId_ShouldReturn200() {
        // Act
        ResponseEntity<?> response = sessionController.noLongerParticipate("1", "1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(sessionService, times(1)).noLongerParticipate(1L, 1L);
    }

    @Test
    @DisplayName("noLongerParticipate with invalid session id should return 400 (Bad Request)")
    public void noLongerParticipate_withInvalidSessionId_ShouldReturn400() {
        // Act
        ResponseEntity<?> response = sessionController.noLongerParticipate("abc", "1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sessionService, never()).noLongerParticipate(anyLong(), anyLong());
    }

    @Test
    @DisplayName("noLongerParticipate with invalid user id should return 400 (Bad Request)")
    public void noLongerParticipate_withInvalidUserId_ShouldReturn400() {
        // Act
        ResponseEntity<?> response = sessionController.noLongerParticipate("1", "abc");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sessionService, never()).noLongerParticipate(anyLong(), anyLong());
    }
}