package test.java.edu.uca.registration.service;

import edu.uca.registration.model.Course;
import edu.uca.registration.model.Student;
import edu.uca.registration.repo.CourseRepository;
import edu.uca.registration.repo.EnrollmentRepository;
import edu.uca.registration.repo.StudentRepository;
import edu.uca.registration.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

// Simple in-memory implementations for unit testing
class TestStudentRepository implements StudentRepository {
    private final Map<String, Student> students = new HashMap<>();

    @Override
    public void save(Student student) {
        students.put(student.id, student);
    }

    @Override
    public Student findById(String bannerId) {
        return students.get(bannerId);
    }

    @Override
    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }

    @Override
    public boolean existsById(String bannerId) {
        return students.containsKey(bannerId);
    }

    @Override
    public Map<String, Student> getAllStudentsMap() {
        return students;
    }
}

class TestCourseRepository implements CourseRepository {
    private final Map<String, Course> courses = new HashMap<>();

    @Override
    public void save(Course course) {
        courses.put(course.code, course);
    }

    @Override
    public Course findByCode(String code) {
        return courses.get(code);
    }

    @Override
    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }

    @Override
    public boolean existsByCode(String code) {
        return courses.containsKey(code);
    }

    @Override
    public Map<String, Course> getAllCoursesMap() {
        return courses;
    }
}

class TestEnrollmentRepository implements EnrollmentRepository {
    @Override
    public void loadEnrollments() {}

    @Override
    public void saveEnrollments() {}
}

class RegistrationServiceUnitTest {

    private StudentRepository studentRepo;
    private CourseRepository courseRepo;
    private EnrollmentRepository enrollmentRepo;
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        studentRepo = new TestStudentRepository();
        courseRepo = new TestCourseRepository();
        enrollmentRepo = new TestEnrollmentRepository();
        registrationService = new RegistrationService(studentRepo, courseRepo, enrollmentRepo);
    }

    @Test
    @DisplayName("UT-01: Add student with valid data")
    void addStudent_ValidData_Success() {
        // Act & Assert - No exception should be thrown
        assertDoesNotThrow(() ->
                registrationService.addStudent("B001", "John Doe", "john@uca.edu"));

        // Verify student was saved
        Student savedStudent = studentRepo.findById("B001");
        assertNotNull(savedStudent);
        assertEquals("John Doe", savedStudent.name);
        assertEquals("john@uca.edu", savedStudent.email);
    }

    @Test
    @DisplayName("UT-02: Add student with invalid banner ID")
    void addStudent_InvalidBannerId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> registrationService.addStudent("001", "John Doe", "john@uca.edu"));

        assertEquals("Banner ID must start with 'B'", exception.getMessage());
    }

    @Test
    @DisplayName("UT-03: Add student with empty name")
    void addStudent_EmptyName_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> registrationService.addStudent("B001", "", "john@uca.edu"));

        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("UT-04: Add course with valid data")
    void addCourse_ValidData_Success() {
        // Act & Assert
        assertDoesNotThrow(() ->
                registrationService.addCourse("CSCI4490", "Software Engineering", 30));

        // Verify course was saved
        Course savedCourse = courseRepo.findByCode("CSCI4490");
        assertNotNull(savedCourse);
        assertEquals("Software Engineering", savedCourse.title);
        assertEquals(30, savedCourse.capacity);
    }

    @Test
    @DisplayName("UT-05: Add course with invalid capacity")
    void addCourse_InvalidCapacity_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> registrationService.addCourse("CSCI4490", "Software Engineering", 0));

        assertEquals("Capacity must be at least 1", exception.getMessage());
    }

    @Test
    @DisplayName("UT-06: Enroll student with empty parameters")
    void enrollStudent_EmptyParameters_ReturnsError() {
        // Act
        String result1 = registrationService.enrollStudent("", "CSCI4490");
        String result2 = registrationService.enrollStudent("B001", "");

        // Assert
        assertEquals("Student ID cannot be empty", result1);
        assertEquals("Course code cannot be empty", result2);
    }

    @Test
    @DisplayName("UT-07: Drop student with empty parameters")
    void dropStudent_EmptyParameters_ReturnsError() {
        // Act
        String result1 = registrationService.dropStudent("", "CSCI4490");
        String result2 = registrationService.dropStudent("B001", "");

        // Assert
        assertEquals("Student ID cannot be empty", result1);
        assertEquals("Course code cannot be empty", result2);
    }

    @Test
    @DisplayName("UT-08: Enroll student in non-existent course")
    void enrollStudent_NonExistentCourse_ReturnsError() {
        // Arrange
        registrationService.addStudent("B001", "John Doe", "john@uca.edu");

        // Act
        String result = registrationService.enrollStudent("B001", "NONEXISTENT");

        // Assert
        assertEquals("No such course", result);
    }

    @Test
    @DisplayName("UT-09: Successful enrollment")
    void enrollStudent_SuccessfulEnrollment_ReturnsEnrolled() {
        // Arrange
        registrationService.addStudent("B001", "John Doe", "john@uca.edu");
        registrationService.addCourse("CSCI4490", "Software Engineering", 30);

        // Act
        String result = registrationService.enrollStudent("B001", "CSCI4490");

        // Assert
        assertEquals("ENROLLED", result);

        Course course = courseRepo.findByCode("CSCI4490");
        assertTrue(course.roster.contains("B001"));
    }
}