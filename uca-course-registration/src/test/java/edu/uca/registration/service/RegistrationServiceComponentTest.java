package test.java.edu.uca.registration.service;

import edu.uca.registration.model.Course;
import edu.uca.registration.model.Student;
import edu.uca.registration.repo.CourseRepository;
import edu.uca.registration.repo.StudentRepository;
import edu.uca.registration.repo.impl.CsvCourseRepository;
import edu.uca.registration.repo.impl.CsvEnrollmentRepository;
import edu.uca.registration.repo.impl.CsvStudentRepository;
import edu.uca.registration.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceComponentTest {

    private StudentRepository studentRepo;
    private CourseRepository courseRepo;
    private CsvEnrollmentRepository enrollmentRepo;
    private RegistrationService registrationService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create temporary CSV files
        tempDir = Files.createTempDirectory("registration-test");
        File studentsFile = new File(tempDir.toFile(), "students.csv");
        File coursesFile = new File(tempDir.toFile(), "courses.csv");
        File enrollmentsFile = new File(tempDir.toFile(), "enrollments.csv");

        // Initialize with test data - create empty files first
        studentsFile.createNewFile();
        coursesFile.createNewFile();
        enrollmentsFile.createNewFile();

        // Create repositories with custom file paths
        studentRepo = new TestCsvStudentRepository(studentsFile.getAbsolutePath());
        courseRepo = new TestCsvCourseRepository(coursesFile.getAbsolutePath());
        enrollmentRepo = new TestCsvEnrollmentRepository(courseRepo, enrollmentsFile.getAbsolutePath());
        registrationService = new RegistrationService(studentRepo, courseRepo, enrollmentRepo);
    }

    // Custom repository implementations that use specific file paths and don't load from default locations
    static class TestCsvStudentRepository extends CsvStudentRepository {
        private final String studentsFile;

        public TestCsvStudentRepository(String studentsFile) {
            this.studentsFile = studentsFile;
            // Don't auto-load - we'll load specific test data in each test
        }


        protected void loadStudents() {
            // Only load from our specific test file, ignore default location
            File f = new File(studentsFile);
            if (!f.exists()) return;
            try (var br = new java.io.BufferedReader(new java.io.FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",", -1);
                    if (p.length >= 3) {
                        getAllStudentsMap().put(p[0], new Student(p[0], p[1], p[2]));
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to load students: " + e.getMessage());
            }
        }


        protected void saveStudents() {
            // Save to our test file only
            File f = new File(studentsFile);
            try (var pw = new java.io.PrintWriter(new java.io.FileWriter(f))) {
                for (Student s : getAllStudentsMap().values()) {
                    pw.println(s.id + "," + s.name + "," + s.email);
                }
            } catch (Exception e) {
                System.out.println("Failed to save students: " + e.getMessage());
            }
        }
    }

    static class TestCsvCourseRepository extends CsvCourseRepository {
        private final String coursesFile;

        public TestCsvCourseRepository(String coursesFile) {
            this.coursesFile = coursesFile;
            // Don't auto-load - we'll load specific test data in each test
        }

        protected void loadCourses() {
            // Only load from our specific test file, ignore default location
            File f = new File(coursesFile);
            if (!f.exists()) return;
            try (var br = new java.io.BufferedReader(new java.io.FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",", -1);
                    if (p.length >= 3) {
                        try {
                            int cap = Integer.parseInt(p[2]);
                            getAllCoursesMap().put(p[0], new Course(p[0], p[1], cap));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to load courses: " + e.getMessage());
            }
        }

        protected void saveCourses() {
            // Save to our test file only
            File f = new File(coursesFile);
            try (var pw = new java.io.PrintWriter(new java.io.FileWriter(f))) {
                for (Course c : getAllCoursesMap().values()) {
                    pw.println(c.code + "," + c.title + "," + c.capacity);
                }
            } catch (Exception e) {
                System.out.println("Failed to save courses: " + e.getMessage());
            }
        }
    }

    static class TestCsvEnrollmentRepository extends CsvEnrollmentRepository {
        private final String enrollmentsFile;

        public TestCsvEnrollmentRepository(CourseRepository courseRepository, String enrollmentsFile) {
            super(courseRepository);
            this.enrollmentsFile = enrollmentsFile;
        }

        @Override
        public void loadEnrollments() {
            File f = new File(enrollmentsFile);
            if (!f.exists()) return;
            try (var br = new java.io.BufferedReader(new java.io.FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split("\\|", -1);
                    if (p.length >= 3) {
                        String code = p[0], sid = p[1], status = p[2];
                        var course = ((TestCsvCourseRepository) getCourseRepository()).findByCode(code);
                        if (course == null) continue;
                        if ("ENROLLED".equalsIgnoreCase(status)) {
                            if (!course.roster.contains(sid)) course.roster.add(sid);
                        } else if ("WAITLIST".equalsIgnoreCase(status)) {
                            if (!course.waitlist.contains(sid)) course.waitlist.add(sid);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to load enrollments: " + e.getMessage());
            }
        }

        @Override
        public void saveEnrollments() {
            File f = new File(enrollmentsFile);
            try (var pw = new java.io.PrintWriter(new java.io.FileWriter(f))) {
                for (var course : ((TestCsvCourseRepository) getCourseRepository()).getAllCoursesMap().values()) {
                    for (String sid : course.roster) pw.println(course.code + "|" + sid + "|ENROLLED");
                    for (String sid : course.waitlist) pw.println(course.code + "|" + sid + "|WAITLIST");
                }
            } catch (Exception e) {
                System.out.println("Failed to save enrollments: " + e.getMessage());
            }
        }

        private CourseRepository getCourseRepository() {
            try {
                var field = CsvEnrollmentRepository.class.getDeclaredField("courseRepository");
                field.setAccessible(true);
                return (CourseRepository) field.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    @DisplayName("CT-01: Enroll student in course with available capacity")
    void enrollStudent_AvailableCapacity_ReturnsEnrolled() {
        // Arrange - Add test data directly to repositories
        studentRepo.save(new Student("B001", "Alice", "alice@uca.edu"));
        courseRepo.save(new Course("CSCI4490", "Software Engineering", 2));
        registrationService.loadAllData();

        // Act
        String result = registrationService.enrollStudent("B001", "CSCI4490");

        // Assert
        assertEquals("ENROLLED", result);

        Course course = courseRepo.findByCode("CSCI4490");
        assertNotNull(course, "Course should not be null");
        assertTrue(course.roster.contains("B001"));
        assertEquals(1, course.roster.size());
    }

    @Test
    @DisplayName("CT-02: Enroll multiple students beyond capacity - waitlist behavior")
    void enrollStudent_BeyondCapacity_ReturnsWaitlist() {
        // Arrange - Add test data directly to repositories
        studentRepo.save(new Student("B001", "Alice", "alice@uca.edu"));
        studentRepo.save(new Student("B002", "Bob", "bob@uca.edu"));
        studentRepo.save(new Student("B003", "Charlie", "charlie@uca.edu"));
        courseRepo.save(new Course("CSCI4490", "Software Engineering", 2));
        registrationService.loadAllData();

        // Act - Enroll first two students
        registrationService.enrollStudent("B001", "CSCI4490");
        registrationService.enrollStudent("B002", "CSCI4490");

        // Act - Try to enroll third student
        String result = registrationService.enrollStudent("B003", "CSCI4490");

        // Assert
        assertEquals("WAITLIST", result);

        Course course = courseRepo.findByCode("CSCI4490");
        assertNotNull(course, "Course should not be null");
        assertEquals(2, course.roster.size());
        assertEquals(1, course.waitlist.size());
        assertTrue(course.waitlist.contains("B003"));
    }

    @Test
    @DisplayName("CT-03: Drop enrolled student promotes waitlisted student")
    void dropStudent_WithWaitlist_PromotesStudent() {
        // Arrange - Add test data directly to repositories
        studentRepo.save(new Student("B001", "Alice", "alice@uca.edu"));
        studentRepo.save(new Student("B002", "Bob", "bob@uca.edu"));
        studentRepo.save(new Student("B003", "Charlie", "charlie@uca.edu"));
        courseRepo.save(new Course("CSCI4490", "Software Engineering", 2));
        registrationService.loadAllData();

        // Fill course and create waitlist
        registrationService.enrollStudent("B001", "CSCI4490");
        registrationService.enrollStudent("B002", "CSCI4490");
        registrationService.enrollStudent("B003", "CSCI4490"); // Waitlisted

        Course courseBefore = courseRepo.findByCode("CSCI4490");
        assertNotNull(courseBefore, "Course should not be null");
        assertEquals(2, courseBefore.roster.size());
        assertEquals(1, courseBefore.waitlist.size());

        // Act
        String result = registrationService.dropStudent("B001", "CSCI4490");

        // Assert
        assertEquals("PROMOTED:B003", result);

        Course courseAfter = courseRepo.findByCode("CSCI4490");
        assertNotNull(courseAfter, "Course should not be null");
        assertEquals(2, courseAfter.roster.size()); // Still at capacity
        assertTrue(courseAfter.roster.contains("B002"));
        assertTrue(courseAfter.roster.contains("B003"));
        assertEquals(0, courseAfter.waitlist.size());
    }

    @Test
    @DisplayName("CT-04: List all students and courses")
    void getAllStudentsAndCourses_ReturnsCorrectLists() {
        // Arrange - Add test data directly to repositories
        studentRepo.save(new Student("B001", "Alice", "alice@uca.edu"));
        studentRepo.save(new Student("B002", "Bob", "bob@uca.edu"));
        studentRepo.save(new Student("B003", "Charlie", "charlie@uca.edu"));
        courseRepo.save(new Course("CSCI4490", "Software Engineering", 2));
        courseRepo.save(new Course("MATH1496", "Calculus I", 50));
        registrationService.loadAllData();

        // Act
        var students = registrationService.getAllStudents();
        var courses = registrationService.getAllCourses();

        // Assert
        assertEquals(3, students.size(), "Should have exactly 3 students");
        assertEquals(2, courses.size(), "Should have exactly 2 courses");

        // Verify student data
        assertEquals("B001", students.get(0).id);
        assertEquals("Alice", students.get(0).name);

        // Verify course data
        assertEquals("CSCI4490", courses.get(0).code);
        assertEquals("Software Engineering", courses.get(0).title);
    }
}