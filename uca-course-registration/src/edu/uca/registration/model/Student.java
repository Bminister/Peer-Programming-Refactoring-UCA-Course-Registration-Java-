package edu.uca.registration.model;

public class Student {
    public String id, name, email;

    public Student(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String toString() {
        return id + " " + name + " <" + email + ">";
    }
}