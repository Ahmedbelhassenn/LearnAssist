package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Chapter;
import com.example.LearnAssist.Models.Course;
import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Repositories.ChapterRepository;
import com.example.LearnAssist.Repositories.CourseRepository;
import com.example.LearnAssist.Repositories.FormationRepository;
import com.example.LearnAssist.Services.CourseServices;
import com.example.LearnAssist.Services.InscriptionFormationServices;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CourseServicesImpl implements CourseServices {
    @Autowired
    private CourseRepository courseRepository;
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
    public void addCourse(Long idFormation,String title, String description, List<HashMap<String, String>> chapters, String email) {
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



                if (chapterMap.get("documentFileName") != null && !chapterMap.get("documentFileName").isEmpty()) {
                    newChapter.setDocumentFileName(chapterMap.get("documentFileName"));
                }

                if (chapterMap.get("videoFileName") != null && !chapterMap.get("videoFileName").isEmpty()) {
                    newChapter.setVideoFileName(chapterMap.get("videoFileName"));
                }

                if(chapterMap.get("quiz") != null && !chapterMap.get("quiz").isEmpty()) {
                    newChapter.setQuiz(chapterMap.get("quiz"));

                }
                //Lier le chapitre au cours
                newChapter.setCourse(newCourse);
                chapterList.add(newChapter);
            }
            newCourse.setChapters(chapterList);
        }

        // Ici, tout est sauvegardé grâce au cascade = ALL
        courseRepository.save(newCourse);
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
    public void deleteCourse(Long id, String email) {
        Course existingCourse=courseRepository.findById(id).orElseThrow(
                ()->  new  ExceptionError("Course not found")
        );
        String InstructorEmail=existingCourse.getFormation().getEmailInstructor();
        if(!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to delete this course");
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
