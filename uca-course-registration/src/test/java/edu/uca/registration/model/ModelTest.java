package test.java.edu.uca.registration.model;

import edu.uca.registration.model.Course;
import edu.uca.registration.model.Student;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    @DisplayName("Student test.java.edu.uca.registration.model creation and toString")
    void studentCreation_ValidData_Success() {
        // Arrange & Act
        Student student = new Student("B001", "John Doe", "john@uca.edu");

        // Assert
        assertEquals("B001", student.id);
        assertEquals("John Doe", student.name);
        assertEquals("john@uca.edu", student.email);
        assertEquals("B001 John Doe <john@uca.edu>", student.toString());
    }

    @Test
    @DisplayName("Course test.java.edu.uca.registration.model creation and toString")
    void courseCreation_ValidData_Success() {
        // Arrange & Act
        Course course = new Course("CSCI4490", "Software Engineering", 30);

        // Assert
        assertEquals("CSCI4490", course.code);
        assertEquals("Software Engineering", course.title);
        assertEquals(30, course.capacity);
        assertEquals(0, course.roster.size());
        assertEquals(0, course.waitlist.size());
        assertTrue(course.toString().contains("CSCI4490 Software Engineering"));
    }

    @Test
    @DisplayName("Course roster and waitlist operations")
    void courseEnrollmentOperations_Success() {
        // Arrange
        Course course = new Course("CSCI4490", "Software Engineering", 2);

        // Act - Add to roster
        course.roster.add("B001");
        course.roster.add("B002");
        course.waitlist.add("B003");

        // Assert
        assertEquals(2, course.roster.size());
        assertEquals(1, course.waitlist.size());
        assertTrue(course.roster.contains("B001"));
        assertTrue(course.waitlist.contains("B003"));

        // Act - Remove from roster
        course.roster.remove("B001");
        course.waitlist.remove("B003");

        // Assert
        assertEquals(1, course.roster.size());
        assertEquals(0, course.waitlist.size());
    }
}