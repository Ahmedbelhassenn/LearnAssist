package com.example.LearnAssist.Services;

import com.example.LearnAssist.Models.Course;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CourseServices {
    Course getCourse(Long id, Principal principal);
    List<Course> getAllCourses();
    /**
     * @param files uploaded chapter files, keyed "chapterVideo{i}" / "chapterDocument{i}"
     *              where i is the chapter index in {@code chapters}
     */
    void addCourse(Long idFormation, String title, String description, List<HashMap<String,String>> chapters,
                   Map<String, MultipartFile> files, String email);
    void updateCourse(Long id, String title, String description, String email);
    void deleteCourse(Long id, String email);
    List<HashMap<String,String>> getCoursesByFormationId(Long id);
}
