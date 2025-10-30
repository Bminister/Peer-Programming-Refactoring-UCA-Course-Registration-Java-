package edu.uca.registration.app;

import edu.uca.registration.repo.impl.CsvCourseRepository;
import edu.uca.registration.repo.impl.CsvEnrollmentRepository;
import edu.uca.registration.repo.impl.CsvStudentRepository;
import edu.uca.registration.service.RegistrationService;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Create repositories
        var studentRepo = new CsvStudentRepository();
        var courseRepo = new CsvCourseRepository();
        var enrollmentRepo = new CsvEnrollmentRepository(courseRepo);

        // Create test.java.edu.uca.registration.service
        var registrationService = new RegistrationService(studentRepo, courseRepo, enrollmentRepo);

        // Load existing data
        registrationService.loadAllData();

        // Check for demo mode
        boolean demo = args.length > 0 && "--demo".equalsIgnoreCase(args[0]);
        if (demo) {
            registrationService.seedDemoData();
        }

        // Start CLI application
        var app = new CourseRegistrationApp(registrationService);
        app.run();

        // Save data on exit
        registrationService.saveAllData();
    }
}