package edu.uca.registration.repo.impl;

import edu.uca.registration.repo.CourseRepository;
import edu.uca.registration.repo.EnrollmentRepository;
import edu.uca.registration.repo.StudentRepository;
import java.io.*;

public class CsvEnrollmentRepository implements EnrollmentRepository {
    private static final String ENROLLMENTS_CSV = "enrollments.csv";
    private final CourseRepository courseRepository;

    public CsvEnrollmentRepository(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void loadEnrollments() {
        File f = new File(ENROLLMENTS_CSV);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 3) {
                    String code = p[0], sid = p[1], status = p[2];
                    var course = courseRepository.findByCode(code);
                    if (course == null) continue;
                    if ("ENROLLED".equalsIgnoreCase(status)) {
                        if (!course.roster.contains(sid)) course.roster.add(sid);
                    } else if ("WAITLIST".equalsIgnoreCase(status)) {
                        if (!course.waitlist.contains(sid)) course.waitlist.add(sid);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed load enrollments: " + e.getMessage());
        }
    }

    @Override
    public void saveEnrollments() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ENROLLMENTS_CSV))) {
            for (var course : courseRepository.getAllCoursesMap().values()) {
                for (String sid : course.roster) pw.println(course.code + "|" + sid + "|ENROLLED");
                for (String sid : course.waitlist) pw.println(course.code + "|" + sid + "|WAITLIST");
            }
        } catch (Exception e) {
            System.out.println("Failed save enrollments: " + e.getMessage());
        }
    }
}