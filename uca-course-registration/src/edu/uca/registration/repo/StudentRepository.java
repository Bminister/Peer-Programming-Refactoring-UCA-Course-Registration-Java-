package edu.uca.registration.repo;

import edu.uca.registration.model.Student;
import java.util.List;
import java.util.Map;

public interface StudentRepository {
    void save(Student student);
    Student findById(String bannerId);
    List<Student> findAll();
    boolean existsById(String bannerId);
    Map<String, Student> getAllStudentsMap();
}