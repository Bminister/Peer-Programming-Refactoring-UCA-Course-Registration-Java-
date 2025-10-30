package test.java.edu.uca.registration.app;

import edu.uca.registration.service.RegistrationService;
import edu.uca.registration.repo.impl.CsvCourseRepository;
import edu.uca.registration.repo.impl.CsvEnrollmentRepository;
import edu.uca.registration.repo.impl.CsvStudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SystemTest {

    @TempDir
    Path tempDir;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() throws Exception {
        // Create EMPTY temporary CSV files to ensure no data is loaded
        File studentsFile = new File(tempDir.toFile(), "students.csv");
        File coursesFile = new File(tempDir.toFile(), "courses.csv");
        File enrollmentsFile = new File(tempDir.toFile(), "enrollments.csv");

        // Create empty files
        studentsFile.createNewFile();
        coursesFile.createNewFile();
        enrollmentsFile.createNewFile();

        // Create repositories that use ONLY these empty files
        var studentRepo = new EmptyCsvStudentRepository(studentsFile.getAbsolutePath());
        var courseRepo = new EmptyCsvCourseRepository(coursesFile.getAbsolutePath());
        var enrollmentRepo = new EmptyCsvEnrollmentRepository(courseRepo, enrollmentsFile.getAbsolutePath());

        registrationService = new RegistrationService(studentRepo, courseRepo, enrollmentRepo);

        // Don't call loadAllData() - we want to start completely fresh
    }

    // Repository implementations that are guaranteed to be empty
    static class EmptyCsvStudentRepository extends CsvStudentRepository {
        private final String studentsFile;

        public EmptyCsvStudentRepository(String studentsFile) {
            this.studentsFile = studentsFile;
            // Start with completely empty data
            clearAllData();
        }

        protected void loadStudents() {
            // Completely override - don't load anything
            clearAllData();
        }

        private void clearAllData() {
            try {
                var field = CsvStudentRepository.class.getDeclaredField("students");
                field.setAccessible(true);
                field.set(this, new java.util.LinkedHashMap<String, edu.uca.registration.model.Student>());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class EmptyCsvCourseRepository extends CsvCourseRepository {
        private final String coursesFile;

        public EmptyCsvCourseRepository(String coursesFile) {
            this.coursesFile = coursesFile;
            // Start with completely empty data
            clearAllData();
        }

        protected void loadCourses() {
            // Completely override - don't load anything
            clearAllData();
        }

        private void clearAllData() {
            try {
                var field = CsvCourseRepository.class.getDeclaredField("courses");
                field.setAccessible(true);
                field.set(this, new java.util.LinkedHashMap<String, edu.uca.registration.model.Course>());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class EmptyCsvEnrollmentRepository extends CsvEnrollmentRepository {
        private final String enrollmentsFile;

        public EmptyCsvEnrollmentRepository(EmptyCsvCourseRepository courseRepository, String enrollmentsFile) {
            super(courseRepository);
            this.enrollmentsFile = enrollmentsFile;
        }

        @Override
        public void loadEnrollments() {
            // Do nothing - start with empty enrollments
        }
    }

    @Test
    @DisplayName("ST-01: Complete workflow - Add → Enroll → Drop → List")
    void completeWorkflow_AddEnrollDropList_Success() {
        // Arrange - Start with completely empty system

        // Act & Assert - Add new student
        assertDoesNotThrow(() ->
                registrationService.addStudent("B999", "Workflow Test", "workflow@uca.edu"));

        // Add course with LARGE capacity to ensure enrollment works
        assertDoesNotThrow(() ->
                registrationService.addCourse("CSCI4490", "Software Engineering", 50)); // Large capacity

        // Enroll student - should work since course is empty and has large capacity
        String enrollResult = registrationService.enrollStudent("B999", "CSCI4490");
        assertEquals("ENROLLED", enrollResult, "Should enroll successfully in empty course");

        // Verify enrollment
        var courses = registrationService.getAllCourses();
        assertEquals(1, courses.size());
        assertTrue(courses.get(0).roster.contains("B999"), "Student should be in roster");

        // Drop student
        String dropResult = registrationService.dropStudent("B999", "CSCI4490");
        assertEquals("DROPPED", dropResult, "Should drop successfully");

        // Verify drop
        courses = registrationService.getAllCourses();
        assertFalse(courses.get(0).roster.contains("B999"), "Student should be removed from roster");

        // List operations should work without errors
        assertDoesNotThrow(() -> {
            var students = registrationService.getAllStudents();
            var coursesList = registrationService.getAllCourses();
            assertEquals(1, students.size());
            assertEquals(1, coursesList.size());
        });
    }

    @Test
    @DisplayName("ST-02: Waitlist promotion workflow")
    void waitlistPromotionWorkflow_Success() {
        // Arrange - Start with completely empty system
        registrationService.addStudent("B100", "Student1", "s1@uca.edu");
        registrationService.addStudent("B101", "Student2", "s2@uca.edu");
        registrationService.addStudent("B102", "Student3", "s3@uca.edu");

        // Create course with SMALL capacity (2) to test waitlist
        registrationService.addCourse("CSCI4490", "Software Engineering", 2);

        // Act - Fill course to capacity
        String result1 = registrationService.enrollStudent("B100", "CSCI4490");
        String result2 = registrationService.enrollStudent("B101", "CSCI4490");
        String result3 = registrationService.enrollStudent("B102", "CSCI4490");

        // Assert waitlist behavior
        assertEquals("ENROLLED", result1, "First student should enroll");
        assertEquals("ENROLLED", result2, "Second student should enroll");
        assertEquals("WAITLIST", result3, "Third student should be waitlisted");

        var course = registrationService.getAllCourses().get(0);
        assertEquals(2, course.roster.size(), "Course should be at capacity");
        assertEquals(1, course.waitlist.size(), "Should have one waitlisted student");
        assertTrue(course.waitlist.contains("B102"), "B102 should be waitlisted");

        // Act - Drop and promote
        String dropResult = registrationService.dropStudent("B100", "CSCI4490");

        // Assert promotion
        assertEquals("PROMOTED:B102", dropResult, "Should promote waitlisted student");

        course = registrationService.getAllCourses().get(0);
        assertEquals(2, course.roster.size(), "Course should still be at capacity");
        assertTrue(course.roster.contains("B101"), "B101 should remain enrolled");
        assertTrue(course.roster.contains("B102"), "B102 should be promoted from waitlist");
        assertEquals(0, course.waitlist.size(), "Waitlist should be empty");
    }

    @Test
    @DisplayName("ST-03: Basic system operations")
    void basicSystemOperations_Success() {
        // Test that basic operations work in isolation
        registrationService.addStudent("B001", "Test Student", "test@uca.edu");
        registrationService.addCourse("TEST101", "Test Course", 10);

        var students = registrationService.getAllStudents();
        var courses = registrationService.getAllCourses();

        assertEquals(1, students.size());
        assertEquals(1, courses.size());
        assertEquals("B001", students.get(0).id);
        assertEquals("TEST101", courses.get(0).code);
    }
}