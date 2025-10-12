package edu.uca.registration.repo.impl;

import edu.uca.registration.model.Student;
import edu.uca.registration.repo.StudentRepository;
import java.io.*;
import java.util.*;

public class CsvStudentRepository implements StudentRepository {
    private static final String STUDENTS_CSV = "students.csv";
    private final Map<String, Student> students = new LinkedHashMap<>();

    public CsvStudentRepository() {
        loadStudents();
    }

    private void loadStudents() {
        File f = new File(STUDENTS_CSV);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 3) {
                    students.put(p[0], new Student(p[0], p[1], p[2]));
                }
            }
        } catch (Exception e) {
            System.out.println("Failed load students: " + e.getMessage());
        }
    }

    private void saveStudents() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(STUDENTS_CSV))) {
            for (Student s : students.values()) {
                pw.println(s.id + "," + s.name + "," + s.email);
            }
        } catch (Exception e) {
            System.out.println("Failed save students: " + e.getMessage());
        }
    }

    @Override
    public void save(Student student) {
        students.put(student.id, student);
        saveStudents();
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