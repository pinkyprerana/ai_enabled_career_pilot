package com.example.learningSpringBootApp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.dto.StudentRequest;
import com.example.learningSpringBootApp.dto.StudentResponse;
import com.example.learningSpringBootApp.entity.StudentEntity;
import com.example.learningSpringBootApp.exception.DuplicateResourceException;
import com.example.learningSpringBootApp.exception.ResourceNotFoundException;
import com.example.learningSpringBootApp.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StudentResponse findById(Long id) {
        StudentEntity student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return mapToResponse(student);
    }

    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Student with email '" + request.email() + "' already exists");
        }
        StudentEntity entity = new StudentEntity(request.name(), request.email());
        StudentEntity saved = studentRepository.save(entity);
        return mapToResponse(saved);
    }

    public StudentResponse update(Long id, StudentRequest request) {
        StudentEntity existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (studentRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Student with email '" + request.email() + "' already exists");
        }

        existing.setName(request.name());
        existing.setEmail(request.email());
        StudentEntity updated = studentRepository.save(existing);
        return mapToResponse(updated);
    }

    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    private StudentResponse mapToResponse(StudentEntity entity) {
        return new StudentResponse(entity.getId(), entity.getName(), entity.getEmail());
    }
}
