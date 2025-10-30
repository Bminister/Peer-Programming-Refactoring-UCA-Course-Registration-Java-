package edu.uca.registration.repo.impl;

import edu.uca.registration.model.Course;
import edu.uca.registration.repo.CourseRepository;
import java.io.*;
import java.util.*;

public class CsvCourseRepository implements CourseRepository {
    private static final String COURSES_CSV = "courses.csv";
    private final Map<String, Course> courses = new LinkedHashMap<>();

    public CsvCourseRepository() {
        loadCourses();
    }

    private void loadCourses() {
        File f = new File(COURSES_CSV);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 3) {
                    try {
                        int cap = Integer.parseInt(p[2]);
                        courses.put(p[0], new Course(p[0], p[1], cap));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println("Failed load courses: " + e.getMessage());
        }
    }

    private void saveCourses() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(COURSES_CSV))) {
            for (Course c : courses.values()) {
                pw.println(c.code + "," + c.title + "," + c.capacity);
            }
        } catch (Exception e) {
            System.out.println("Failed save courses: " + e.getMessage());
        }
    }

    @Override
    public void save(Course course) {
        courses.put(course.code, course);
        saveCourses();
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