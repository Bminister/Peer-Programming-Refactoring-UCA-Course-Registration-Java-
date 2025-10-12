package edu.uca.registration.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    public String code, title;
    public int capacity;
    public List<String> roster = new ArrayList<>();
    public List<String> waitlist = new ArrayList<>();

    public Course(String code, String title, int capacity) {
        this.code = code;
        this.title = title;
        this.capacity = capacity;
    }

    public String toString() {
        return code + " " + title + " cap=" + capacity +
                " enrolled=" + roster.size() + " wait=" + waitlist.size();
    }
}