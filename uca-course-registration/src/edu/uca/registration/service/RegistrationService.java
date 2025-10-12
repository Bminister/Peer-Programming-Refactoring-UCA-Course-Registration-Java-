package edu.uca.registration.service;

import edu.uca.registration.model.Course;
import edu.uca.registration.model.Student;
import edu.uca.registration.repo.CourseRepository;
import edu.uca.registration.repo.EnrollmentRepository;
import edu.uca.registration.repo.StudentRepository;
import java.util.List;

public class RegistrationService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public RegistrationService(StudentRepository studentRepository,
                               CourseRepository courseRepository,
                               EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    private void validateStudentInput(String bannerId, String name, String email) {
        if (bannerId == null || bannerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Banner ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!bannerId.toUpperCase().startsWith("B")) {
            throw new IllegalArgumentException("Banner ID must start with 'B'");
        }
    }

    private void validateCourseInput(String code, String title, int capacity) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }
        if (capacity > 500) {
            throw new IllegalArgumentException("Capacity cannot exceed 500");
        }
    }

    public void addStudent(String bannerId, String name, String email) {
        validateStudentInput(bannerId, name, email);

        Student s = new Student(bannerId, name, email);
        studentRepository.save(s);
    }

    public void addCourse(String code, String title, int capacity) {
        validateCourseInput(code, title, capacity);

        Course c = new Course(code, title, capacity);
        courseRepository.save(c);
    }

    public String enrollStudent(String studentId, String courseCode) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return "Student ID cannot be empty";
        }
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return "Course code cannot be empty";
        }

        Course c = courseRepository.findByCode(courseCode);
        if (c == null) return "No such course";
        if (c.roster.contains(studentId)) return "Already enrolled";
        if (c.waitlist.contains(studentId)) return "Already waitlisted";

        if (c.roster.size() >= c.capacity) {
            c.waitlist.add(studentId);
            courseRepository.save(c);
            return "WAITLIST";
        } else {
            c.roster.add(studentId);
            courseRepository.save(c);
            return "ENROLLED";
        }
    }

    public String dropStudent(String studentId, String courseCode) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return "Student ID cannot be empty";
        }
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return "Course code cannot be empty";
        }

        Course c = courseRepository.findByCode(courseCode);
        if (c == null) return "No such course";

        if (c.roster.remove(studentId)) {
            // Promote first waitlisted (FIFO)
            if (!c.waitlist.isEmpty()) {
                String promote = c.waitlist.removeFirst();
                c.roster.add(promote);
                courseRepository.save(c);
                return "PROMOTED:" + promote;
            } else {
                courseRepository.save(c);
                return "DROPPED";
            }
        } else if (c.waitlist.remove(studentId)) {
            courseRepository.save(c);
            return "WAITLIST_REMOVED";
        } else {
            return "NOT_ENROLLED";
        }
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public void loadAllData() {
        enrollmentRepository.loadEnrollments();
    }

    public void saveAllData() {
        enrollmentRepository.saveEnrollments();
    }

    public void seedDemoData() {
        studentRepository.save(new Student("B001", "Alice", "alice@uca.edu"));
        studentRepository.save(new Student("B002", "Brian", "brian@uca.edu"));
        courseRepository.save(new Course("CSCI4490", "Software Engineering", 2));
        courseRepository.save(new Course("MATH1496", "Calculus I", 50));
    }
}