package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Dto.CourseDto;
import com.example.LearnAssist.Models.Course;
import com.example.LearnAssist.Services.CourseServices;
import com.example.LearnAssist.Services.FileServices;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/courses")
@CrossOrigin("http://localhost:4200")
public class CourseController {

    @Autowired
    CourseServices courseServices;
    @Autowired
    FileServices fileServices;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Course> getAllCourses() {
        return courseServices.getAllCourses();
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR', 'PARTICIPANT')")
    @GetMapping("{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id, Principal principal) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            Course course=courseServices.getCourse(id, principal);
            response.put("course", course);
            return  ResponseEntity.ok(response);
        }catch (Exception exceptionError) {
            response.put("error", exceptionError);
            return  ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping(path = "{idFormation}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addCourse(
            @PathVariable Long idFormation,
            @RequestPart("title") String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart("chapters") String chaptersJson,
            @RequestParam Map<String, MultipartFile> files, Principal principal
    ) {
        HashMap<String, String> response = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<HashMap<String, String>> chapters = objectMapper.readValue(chaptersJson, new TypeReference<>() {});

            for (int i = 0; i < chapters.size(); i++) {
                HashMap<String, String> chapter = chapters.get(i);

                MultipartFile video = files.get("chapterVideo" + i);
                MultipartFile document = files.get("chapterDocument" + i);

                if (video != null && !video.isEmpty()) {
                    String videoFileName = fileServices.saveVideoFile(video);
                    chapter.put("videoFileName", videoFileName);
                }

                if (document != null && !document.isEmpty()) {
                    String documentFileName = fileServices.saveDocumentFile(document);
                    chapter.put("documentFileName", documentFileName);
                }
            }
            String email = principal.getName();
            courseServices.addCourse(idFormation, title, description, chapters, email);
            response.put("message", "Course added successfully");
            return ResponseEntity.ok(response);

        } catch (ExceptionError e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("message", "Error while parsing chapters data");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id, Principal principal) {
        HashMap<String,String> hashMap= new HashMap<>();
        try {
            String email = principal.getName();
            courseServices.deleteCourse(id, email);
            hashMap.put("message", "Course deleted successfully");
            return ResponseEntity.ok(hashMap);
        }
        catch (Exception error){
            hashMap.put("error",error.getMessage());
            return ResponseEntity.badRequest().body(hashMap);
        }

    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody CourseDto body, Principal principal){
        HashMap<String, Object> response = new HashMap<>();
        try {
            String email = principal.getName();
            courseServices.updateCourse(id, body.getTitle(), body.getDescription(), email);
            response.put("message", "Course updated successfully");
            return ResponseEntity.ok(response);
        }catch (ExceptionError exceptionError) {
            response.put("message", exceptionError.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR', 'PARTICIPANT')")
    @GetMapping("/formation/{idFormation}")
    public List<HashMap<String,String>> getCourseByFormationId(@PathVariable Long idFormation) {
        try {
            return courseServices.getCoursesByFormationId(idFormation);
        }catch (ExceptionError e) {
            return List.of();
        }
    }
}
