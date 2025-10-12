package edu.uca.registration.repo;

import edu.uca.registration.model.Course;
import java.util.List;
import java.util.Map;

public interface CourseRepository {
    void save(Course course);
    Course findByCode(String code);
    List<Course> findAll();
    boolean existsByCode(String code);
    Map<String, Course> getAllCoursesMap();
}