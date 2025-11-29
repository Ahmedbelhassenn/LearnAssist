package com.example.LearnAssist.Controllers;


import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Chapter;
import com.example.LearnAssist.Models.Course;
import com.example.LearnAssist.Services.ChapterServices;
import com.example.LearnAssist.Services.CourseServices;
import com.example.LearnAssist.Services.FileServices;
import com.example.LearnAssist.Services.FormationServices;
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
@RequestMapping("api/chapters")
public class ChapterController {
    @Autowired
    ChapterServices chapterServices;
    @Autowired
    FileServices fileServices;
    @Autowired
    CourseServices courseServices;
    @Autowired
    FormationServices formationServices;


    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getChapter(@PathVariable Long id, Principal principal) {
        HashMap <String, Object> response = new HashMap<>();

        try {

            Chapter chapter=chapterServices.getChapter(id, principal);
            response.put("chapter", chapter);
            return ResponseEntity.ok(response);

        } catch (Exception e){
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Chapter> getAllChapters() {
        return chapterServices.getAllChapters();
    }


    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping(path = "/{idCourse}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addChapter(
            @PathVariable Long idCourse,
            @RequestPart("title") String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "quiz", required = false) String quiz,
            @RequestParam Map<String, MultipartFile> files, Principal principal
    ) {
        HashMap<String, String> response = new HashMap<>();
        Chapter chapter=new Chapter();
        try {
            chapter.setTitle(title);
            chapter.setDescription(description);
            chapter.setQuiz(quiz);
            Course course=courseServices.getCourse(idCourse, principal);
            chapter.setCourse(course);
            MultipartFile video=files.get("video");
            if (video != null && !video.isEmpty()) {
                String videoFileName = fileServices.saveVideoFile(video);
                chapter.setVideoFileName(videoFileName);
            }
            MultipartFile document=files.get("document");
            if (document != null && !document.isEmpty()) {
                String documentFileName = fileServices.saveDocumentFile(document);
                chapter.setDocumentFileName(documentFileName);
            }
            String email=principal.getName();
            chapterServices.addChapter(chapter, email);
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

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PatchMapping(path = "/{idChapter}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editChapter(
            @PathVariable Long idChapter,
            @RequestPart("title") String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "quiz", required = false) String quiz,
            @RequestParam Map<String, MultipartFile> files, Principal principal
    ) {
        HashMap<String, String> response = new HashMap<>();
        Chapter chapter=new Chapter();
        try {
            chapter.setTitle(title);
            chapter.setDescription(description);
            chapter.setQuiz(quiz);
            MultipartFile video=files.get("video");
            if (video != null && !video.isEmpty()) {
                String videoFileName = fileServices.saveVideoFile(video);
                chapter.setVideoFileName(videoFileName);
            }
            MultipartFile document=files.get("document");
            if (document != null && !document.isEmpty()) {
                String documentFileName = fileServices.saveDocumentFile(document);
                chapter.setDocumentFileName(documentFileName);
            }
            String email=principal.getName();
            chapterServices.editChapter(idChapter,chapter, email);
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
    public ResponseEntity<?> deleteChapter(@PathVariable Long id , Principal principal) {

        HashMap<String, String> response = new HashMap<>();
        try {
            String email=principal.getName();
            chapterServices.deleteChapter(id, email);
            response.put("message", "Course deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (ExceptionError e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/videos/total")
    public ResponseEntity<?> getTotalVideos(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            int result = chapterServices.getInstructorTotalVideosChapters(principal)+
                    formationServices.getInstructorTotalVideosFormation(principal);
            response.put("result", result );
            return ResponseEntity.ok(response);
        }catch (ExceptionError e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/resources/total")
    public ResponseEntity<?> getTotalResources(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            int result = chapterServices.getInstructorTotalResourcesChapters(principal);
            response.put("result", result );
            return ResponseEntity.ok(response);
        }catch (ExceptionError e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
