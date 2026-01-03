package com.openclassrooms.starterjwt.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherController Unit Test")
public class TeacherControllerTest {
    @Mock
    private TeacherService teacherService;
    
    @Mock
    private TeacherMapper teacherMapper;
    
    @InjectMocks
    private TeacherController teacherController;
    
    // ***** FIND BY ID *****
    @Test
    @DisplayName("findById() with existing id should return 200 and TeacherDto")
    public void findById_withExistingId_shouldReturn200AndTeacherDto() {
        // Arrange
        Teacher teacher = new Teacher()
            .setId(1L)
            .setFirstName("Maya")
            .setLastName("Labeille");
        
        TeacherDto teacherDto = new TeacherDto();
        teacherDto.setId(1L);
        teacherDto.setFirstName("Maya");
        teacherDto.setLastName("Labeille");
        
        when(teacherService.findById(1L)).thenReturn(teacher);
        when(teacherMapper.toDto(teacher)).thenReturn(teacherDto);
        
        // Act
        ResponseEntity<?> response = teacherController.findById("1");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(teacherDto);
        verify(teacherService, times(1)).findById(1L);
        verify(teacherMapper, times(1)).toDto(teacher);
    }

    @Test
    @DisplayName("findById() with unknown id should return 404")
    public void findById_withUnknownId_shouldReturn404() {
        // Arrange
        when(teacherService.findById(666L)).thenReturn(null);
        
        // Act
        ResponseEntity<?> response = teacherController.findById("666");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(teacherService, times(1)).findById(666L);
        verify(teacherMapper, never()).toDto(any(Teacher.class));
    }

    @Test
    @DisplayName("findById() with invalid id should return 400")
    public void findById_withInvalidId_shouldReturn400() {
        // Act
        ResponseEntity<?> response = teacherController.findById("abc");
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(teacherService, never()).findById(anyLong());
    }

    // ***** FIND ALL *****
    @Test
    @DisplayName("findAll() should return 200 and list of TeacherDto")
    public void findAll_shouldReturn200AndListOfTeacherDto() {
        // Arrange
        Teacher teacher1 = new Teacher()
            .setId(1L)
            .setFirstName("Maya")
            .setLastName("Labeille");
        Teacher teacher2 = new Teacher()
            .setId(2L)
            .setFirstName("Bob")
            .setLastName("Léponge");
        
        TeacherDto teacherDto1 = new TeacherDto();
        teacherDto1.setId(1L);
        teacherDto1.setFirstName("Maya");
        teacherDto1.setLastName("Labeille");
        TeacherDto teacherDto2 = new TeacherDto();
        teacherDto2.setId(2L);
        teacherDto2.setFirstName("Bob");
        teacherDto2.setLastName("Léponge");
        
        List<Teacher> teachers = List.of(teacher1, teacher2);
        List<TeacherDto> teacherDtos = List.of(teacherDto1, teacherDto2);
        
        when(teacherService.findAll()).thenReturn(teachers);
        when(teacherMapper.toDto(teachers)).thenReturn(teacherDtos);
        
        // Act
        ResponseEntity<?> response = teacherController.findAll();
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(teacherDtos);
        verify(teacherService, times(1)).findAll();
        verify(teacherMapper, times(1)).toDto(teachers);
    }
}