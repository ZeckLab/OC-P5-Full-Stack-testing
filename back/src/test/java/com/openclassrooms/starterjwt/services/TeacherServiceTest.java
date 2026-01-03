package com.openclassrooms.starterjwt.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Test")
public class TeacherServiceTest {
    @Mock
    private TeacherRepository teacherRepository;
    
    @InjectMocks
    private TeacherService teacherService;
    
    private Teacher teacher;
    private Teacher teacher2;
    private List<Teacher> teacherList;
    
    @BeforeEach
    public void prepareTestData() {
        teacher = new Teacher()
            .setId(1L)
            .setFirstName("Maya")
            .setLastName("Labeille");
        
        teacher2 = new Teacher()
            .setId(2L)
            .setFirstName("Bob")
            .setLastName("Léponge");
        
        teacherList = List.of(teacher, teacher2);
    }

    // ***** FIND ALL *****
    @Test
    @DisplayName("findAll() should return all teachers")
    public void findAll_shouldReturnAllTeachers() {
        // Arrange
        when(teacherRepository.findAll()).thenReturn(teacherList);
        
        // Act
        List<Teacher> foundAllTeachers = teacherService.findAll();
        
        // Assert
        assertThat(foundAllTeachers).isEqualTo(teacherList);
        verify(teacherRepository, times(1)).findAll();
    }

    // ***** FIND BY ID *****
    @Test
    @DisplayName("findById() with existing id should return teacher")
    public void findById_withExistingId_shouldReturnTeacher() {
        // Arrange
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        
        // Act
        Teacher foundTeacher = teacherService.findById(1L);
        
        // Assert
        assertThat(foundTeacher).isEqualTo(teacher);
        verify(teacherRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById() with unknown id should return null")
    public void findById_withUnknownId_shouldReturnNull() {
        // Arrange
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act
        Teacher foundTeacher = teacherService.findById(99L);
        
        // Assert
        assertThat(foundTeacher).isNull();
        verify(teacherRepository, times(1)).findById(99L);
    }
}
