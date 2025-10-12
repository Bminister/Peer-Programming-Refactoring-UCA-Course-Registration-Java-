package edu.uca.registration.app;

import edu.uca.registration.service.RegistrationService;
import edu.uca.registration.model.Course;
import edu.uca.registration.model.Student;
import java.util.Scanner;

public class CourseRegistrationApp {
    private final RegistrationService registrationService;
    private final Scanner scanner;

    public CourseRegistrationApp(RegistrationService registrationService) {
        this.registrationService = registrationService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        println("=== UCA Course Registration (Refactored) ===");
        menuLoop();
        println("Goodbye!");
    }

    private void menuLoop() {
        while (true) {
            println("\nMenu:");
            println("1) Add student");
            println("2) Add course");
            println("3) Enroll student in course");
            println("4) Drop student from course");
            println("5) List students");
            println("6) List courses");
            println("0) Exit");
            print("Choose: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": addStudentUI(); break;
                case "2": addCourseUI(); break;
                case "3": enrollUI(); break;
                case "4": dropUI(); break;
                case "5": listStudents(); break;
                case "6": listCourses(); break;
                case "0": return;
                default: println("Invalid"); break;
            }
        }
    }

    private void addStudentUI() {
        print("Banner ID: ");
        String id = scanner.nextLine().trim();
        print("Name: ");
        String name = scanner.nextLine().trim();
        print("Email: ");
        String email = scanner.nextLine().trim();

        try {
            registrationService.addStudent(id, name, email);
            println("Student added successfully.");
        } catch (IllegalArgumentException e) {
            println("Error: " + e.getMessage());
        }
    }

    private void addCourseUI() {
        print("Course Code: ");
        String code = scanner.nextLine().trim();
        print("Title: ");
        String title = scanner.nextLine().trim();
        print("Capacity: ");
        String capacityStr = scanner.nextLine().trim();

        try {
            int cap = Integer.parseInt(capacityStr);
            registrationService.addCourse(code, title, cap);
            println("Course added successfully.");
        } catch (NumberFormatException e) {
            println("Error: Capacity must be a number");
        } catch (IllegalArgumentException e) {
            println("Error: " + e.getMessage());
        }
    }

    private void enrollUI() {
        print("Student ID: ");
        String sid = scanner.nextLine().trim();
        print("Course Code: ");
        String cc = scanner.nextLine().trim();

        String result = registrationService.enrollStudent(sid, cc);

        if (result.contains("cannot be empty")) {
            println("Error: " + result);
        } else {
            switch (result) {
                case "No such course": println("No such course"); break;
                case "Already enrolled": println("Already enrolled"); break;
                case "Already waitlisted": println("Already waitlisted"); break;
                case "WAITLIST": println("Course full. Added to WAITLIST."); break;
                case "ENROLLED": println("Enrolled."); break;
                default: println(result);
            }
        }
    }

    private void dropUI() {
        print("Student ID: ");
        String sid = scanner.nextLine().trim();
        print("Course Code: ");
        String cc = scanner.nextLine().trim();

        String result = registrationService.dropStudent(sid, cc);

        if (result.contains("cannot be empty")) {
            println("Error: " + result);
        } else {
            switch (result) {
                case "No such course": println("No such course"); break;
                case "PROMOTED":
                    String[] parts = result.split(":");
                    println("Promoted " + parts[1] + " from waitlist.");
                    break;
                case "DROPPED": println("Dropped."); break;
                case "WAITLIST_REMOVED": println("Removed from waitlist."); break;
                case "NOT_ENROLLED": println("Not enrolled or waitlisted."); break;
                default: println(result);
            }
        }
    }

    private void listStudents() {
        println("Students:");
        for (Student s : registrationService.getAllStudents()) {
            println(" - " + s);
        }
    }

    private void listCourses() {
        println("Courses:");
        for (Course c : registrationService.getAllCourses()) {
            println(" - " + c);
        }
    }

    private void print(String s){ System.out.print(s); }
    private void println(String s){ System.out.println(s); }
}