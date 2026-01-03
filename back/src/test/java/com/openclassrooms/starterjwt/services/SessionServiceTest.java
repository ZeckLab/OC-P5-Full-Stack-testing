package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService Test")
public class SessionServiceTest {
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private SessionService sessionService;
    
    private Teacher teacher;
    private Session sessionWithoutId;
    private Session sessionWithId;
    
    @BeforeEach
    public void prepareTestData() {
        Date date = new Date();
        List<User> users = new ArrayList<>();
        
        teacher = new Teacher()
            .setId(1L)
            .setFirstName("Maya")
            .setLastName("Labeille");
        
        sessionWithoutId = new Session()
            .setId(null)
            .setName("Yoga")
            .setDescription("Cours de yoga pour débutants")
            .setTeacher(teacher)
            .setDate(date)
            .setUsers(users);
        
        sessionWithId = new Session()
            .setId(1L)
            .setName("Yoga")
            .setDescription("Cours de yoga pour débutants")
            .setTeacher(teacher)
            .setDate(date)
            .setUsers(users);
    }

    // ***** CREATE *****

    @Test
    @DisplayName("create() with valid session should save session")
    public void create_withValidSession_shouldSaveSession() {
        // Arrange
        when(sessionRepository.save(sessionWithoutId)).thenReturn(sessionWithId);
        
        // Act
        Session createdSession = sessionService.create(sessionWithoutId);
        
        // Assert
        assertThat(createdSession).isEqualTo(sessionWithId);
        verify(sessionRepository, times(1)).save(sessionWithoutId);
    }

    // ***** DELETE *****

    @Test
    @DisplayName("delete() should call repository to delete session by id")
    public void delete_withId_shouldCallRepository() {
        // Act
        sessionService.delete(1L);
        
        // Assert
        verify(sessionRepository, times(1)).deleteById(1L);
    }

    // ***** FIND ALL *****

    @Test
    @DisplayName("findAll() should return all sessions")
    public void findAll_shouldReturnAllSessions() {
        // Arrange
        Session session2 = new Session()
                .setId(2L)
                .setName("Pilates")
                .setDescription("Cours de Pilates")
                .setTeacher(teacher)
                .setDate(new Date())
                .setUsers(new ArrayList<>());
        List<Session> sessionList = List.of(sessionWithId, session2);
        when(sessionRepository.findAll()).thenReturn(sessionList);
        
        // Act
        List<Session> foundAllSessions = sessionService.findAll();

        // Assert
        assertThat(foundAllSessions).isEqualTo(sessionList);
        verify(sessionRepository, times(1)).findAll();
    }

    // ***** GET BY ID *****

    @Test
    @DisplayName("getById() with existing id should return session")
    public void getById_withExistingId_shouldReturnSession() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionWithId));
        
        // Act
        Session foundSession = sessionService.getById(1L);
        
        // Assert
        assertThat(foundSession).isEqualTo(sessionWithId);
        verify(sessionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getById() with unknown id should return null")
    public void getById_withUnknownId_shouldReturnNull() {
        // Arrange
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act
        Session foundSession = sessionService.getById(99L);
        
        // Assert
        assertThat(foundSession).isNull();
        verify(sessionRepository, times(1)).findById(99L);
    }

    // ***** UPDATE *****

    @Test
    @DisplayName("update() with valid session and id should set id and save session")
    public void update_withValidSessionAndId_shouldSetIdAndSaveSession() {
        // Arrange
        // Session to update does not have an ID
        Session sessionToUpdate = sessionWithoutId;
        // Expected session (after update) has the ID set
        Session updatedSession = sessionWithId;
        when(sessionRepository.save(any(Session.class))).thenReturn(updatedSession);
        
        // Act
        Session result = sessionService.update(1L, sessionToUpdate);
        
        // Assert
        // Capture the session passed to save() to ensure the ID was injected before saving
        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();
        
        assertThat(savedSession.getId()).isEqualTo(1L);
        assertThat(result).isEqualTo(updatedSession);
    }

    // ****** PARTICIPATE ******

    @Test
    @DisplayName("participate() with missing session should throw NotFoundException")
    public void participate_withMissingSession_shouldThrowNotFound() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        
        // Act + Assert
        assertThatThrownBy(() -> sessionService.participate(1L, 1L))
                .isInstanceOf(NotFoundException.class);
        
        verify(sessionRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    @DisplayName("participate() with missing user should throw NotFoundException")
    public void participate_withMissingUser_shouldThrowNotFound() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(new Session()));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act + Assert
        assertThatThrownBy(() -> sessionService.participate(1L, 1L))
                .isInstanceOf(NotFoundException.class);
        
        verify(sessionRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    @DisplayName("participate() with already participating user should throw BadRequestException")
    public void participate_WithAlreadyParticipatingUser_shouldThrowBadRequest() {
        // Arrange
        User user = new User()
            .setId(1L).setEmail("test@test.com")
            .setFirstName("Hugo").setLastName("Lebolide")
            .setAdmin(false).setPassword("password");
        
        // User already in session → business rule: cannot participate twice
        sessionWithId.setUsers(List.of(user));
        
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionWithId));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Act + Assert
        assertThatThrownBy(() -> sessionService.participate(1L, 1L))
                .isInstanceOf(BadRequestException.class);
        
        verify(sessionRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    @DisplayName("participate() with valid session and user should add user and save")
    public void participate_withValidSessionAndUser_shouldAddUserAndSave() {
        // Arrange
        User user = new User()
            .setId(1L).setEmail("test@test.com")
            .setFirstName("Hugo").setLastName("Lebolide")
            .setAdmin(false).setPassword("password");
        
        // Ensure the session initially has no users
        assertThat(sessionWithId.getUsers()).isEmpty();
        
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionWithId));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Act
        sessionService.participate(1L, 1L);
        
        // Assert
        // Capture the session actually passed to save()
        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();
        
        assertThat(savedSession.getUsers()).contains(user);
        verify(sessionRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
    }

    // ****** NO LONGER PARTICIPATE ******

    @Test
    @DisplayName("noLongerParticipate() with missing session should throw NotFoundException")
    public void noLongerParticipate_withMissingSession_shouldThrowNotFound() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act + Assert
        assertThatThrownBy(() -> sessionService.noLongerParticipate(1L, 1L))
                .isInstanceOf(NotFoundException.class);
        
        verify(sessionRepository, times(1)).findById(1L);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    @DisplayName("noLongerParticipate() with user not participating should throw BadRequestException")
    public void noLongerParticipate_withUserNotParticipating_shouldThrowBadRequest() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionWithId));
        
        // User not in session → business rule: cannot remove a non-participant
        
        // Act + Assert
        assertThatThrownBy(() -> sessionService.noLongerParticipate(1L, 1L))
                .isInstanceOf(BadRequestException.class);
        
        verify(sessionRepository, times(1)).findById(1L);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    @DisplayName("noLongerParticipate() with valid session and user should remove user and save")
    public void noLongerParticipate_withValidSessionAndUser_shouldRemoveUserAndSave() {
        // Arrange
        User user = new User()
            .setId(1L).setEmail("test@test.com")
            .setFirstName("Hugo").setLastName("Lebolide")
            .setAdmin(false).setPassword("password");
        
        User user2 = new User()
            .setId(2L).setEmail("test2@test.com")
            .setFirstName("Sam").setLastName("Lechasseur")
            .setAdmin(false).setPassword("password");
        
        sessionWithId.setUsers(new ArrayList<>(List.of(user, user2)));
        
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionWithId));
        
        // Act
        sessionService.noLongerParticipate(1L, 1L);
        
        // Assert
        // Capture the session actually passed to save()
        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();
        
        assertThat(savedSession.getUsers()).doesNotContain(user);
        verify(sessionRepository, times(1)).findById(1L);
    }
}