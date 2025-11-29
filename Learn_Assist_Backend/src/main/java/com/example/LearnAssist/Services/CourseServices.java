package com.example.LearnAssist.Services;

import com.example.LearnAssist.Models.Course;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

public interface CourseServices {
    Course getCourse(Long id, Principal principal);
    List<Course> getAllCourses();
    void addCourse(Long idFormation,String title, String description, List<HashMap<String,String>> chapters, String email);
    void updateCourse(Long id, String title, String description, String email);
    void deleteCourse(Long id, String email);
    List<HashMap<String,String>> getCoursesByFormationId(Long id);
}
