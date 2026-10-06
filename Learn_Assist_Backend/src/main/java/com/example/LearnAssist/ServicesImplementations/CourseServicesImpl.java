package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Chapter;
import com.example.LearnAssist.Models.Course;
import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Repositories.ChapterRepository;
import com.example.LearnAssist.Repositories.CourseRepository;
import com.example.LearnAssist.Repositories.FormationRepository;
import com.example.LearnAssist.Services.CourseServices;
import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.Services.InscriptionFormationServices;
import com.example.LearnAssist.Storage.FileCategory;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseServicesImpl implements CourseServices {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private InscriptionFormationServices inscriptionFormationServices;
    @Autowired
    private FormationRepository formationRepository;

    @Override
    public Course getCourse(Long id, Principal principal) {
        Course course= courseRepository.findById(id).orElseThrow(
                () -> new ExceptionError("Course with id " + id + " not found")
        );
        Authentication authentication = (Authentication) principal ;
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_INSTRUCTOR")){
            return course;
        }
        Long idFormation = course.getFormation().getId();
        if (inscriptionFormationServices.isParticipantApproved(idFormation, principal)) {
            return course;
        }
        else {
            throw new ExceptionError("Course with id " + id + " is not approved");
        }
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public void addCourse(Long idFormation, String title, String description, List<HashMap<String, String>> chapters,
                          Map<String, MultipartFile> files, String email) {
        Formation formation = formationRepository.findById(idFormation)
                .orElseThrow(() -> new ExceptionError("Formation not found"));
        String InstructorEmail=formation.getEmailInstructor();
        if(!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to add" +
                    " this course");
        }
        if (courseRepository.existsByTitle(title)) {
            throw new ExceptionError("Course with this title already exists");
        }
        Course newCourse = new Course();
        newCourse.setTitle(title);
        newCourse.setFormation(formation);
        if (description != null && !description.isEmpty()) {
            newCourse.setDescription(description);
        }
        List<Chapter> chapterList = new ArrayList<>();
        if (chapters != null && !chapters.isEmpty()) {
            for (HashMap<String, String> chapterMap : chapters) {
                if (chapterRepository.existsByTitle(chapterMap.get("title"))) {
                    throw new ExceptionError("Chapter with this title already exists");
                }
                Chapter newChapter = new Chapter();
                newChapter.setTitle(chapterMap.get("title"));

                if (chapterMap.get("description") != null && !chapterMap.get("description").isEmpty()) {
                    newChapter.setDescription(chapterMap.get("description"));
                }

                // File names are never taken from the client JSON ("videoFileName" /
                // "documentFileName" keys are ignored): they only come from real uploads below.

                if(chapterMap.get("quiz") != null && !chapterMap.get("quiz").isEmpty()) {
                    newChapter.setQuiz(chapterMap.get("quiz"));

                }
                //Lier le chapitre au cours
                newChapter.setCourse(newCourse);
                chapterList.add(newChapter);
            }
            newCourse.setChapters(chapterList);
        }

        // Ownership and titles are validated: the uploaded files can now be stored.
        List<String[]> storedFiles = new ArrayList<>();
        try {
            for (int i = 0; i < chapterList.size(); i++) {
                Chapter chapter = chapterList.get(i);
                String video = fileStorageService.storeIfPresent(
                        files == null ? null : files.get("chapterVideo" + i), FileCategory.VIDEO);
                if (video != null) {
                    storedFiles.add(new String[]{FileCategory.VIDEO.name(), video});
                    chapter.setVideoFileName(video);
                }
                String document = fileStorageService.storeIfPresent(
                        files == null ? null : files.get("chapterDocument" + i), FileCategory.DOCUMENT);
                if (document != null) {
                    storedFiles.add(new String[]{FileCategory.DOCUMENT.name(), document});
                    chapter.setDocumentFileName(document);
                }
            }
            // Ici, tout est sauvegardé grâce au cascade = ALL
            courseRepository.save(newCourse);
        } catch (RuntimeException e) {
            for (String[] stored : storedFiles) {
                fileStorageService.delete(FileCategory.valueOf(stored[0]), stored[1]);
            }
            throw e;
        }
    }


    @Override
    public void updateCourse(Long id, String title, String description, String email) {
        Course existingCourse=courseRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Course not found")
        );
        String InstructorEmail=existingCourse.getFormation().getEmailInstructor();
        if(!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to edit this course");
        }
        if(!title.equals(existingCourse.getTitle())) {
            if(courseRepository.existsByTitle(title)) {
                throw new ExceptionError("Course with this title already exists");
            }
        }
        existingCourse.setTitle(title);
        if(description!=null){
            existingCourse.setDescription(description);
        }

        courseRepository.save(existingCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id, String email) {
        Course existingCourse=courseRepository.findById(id).orElseThrow(
                ()->  new  ExceptionError("Course not found")
        );
        String InstructorEmail=existingCourse.getFormation().getEmailInstructor();
        if(!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to delete this course");
        }
        if (existingCourse.getChapters() != null) {
            // Deferred until the transaction commits (see FileStorageService#delete).
            for (Chapter chapter : existingCourse.getChapters()) {
                fileStorageService.delete(FileCategory.VIDEO, chapter.getVideoFileName());
                fileStorageService.delete(FileCategory.DOCUMENT, chapter.getDocumentFileName());
            }
        }
        courseRepository.deleteById(id);
    }

    @Override
    public List<HashMap<String,String>> getCoursesByFormationId(Long id) {
        List<HashMap<String,String>> courses = new ArrayList<>();
        List<Course>  allCourses= courseRepository.findAll();
        if(!allCourses.isEmpty()) {
            for (Course c : allCourses) {
                HashMap<String,String> course = new HashMap<>();
                if (c.getFormation().getId().equals(id)) {
                    course.put("title", c.getTitle());
                    course.put("id", String.valueOf(c.getId()));
                    courses.add(course);
                }
            }
        }
        return courses;

    }

}
