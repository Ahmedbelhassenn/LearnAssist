package com.example.LearnAssist.Services;


import com.example.LearnAssist.Models.Instructor;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface InstructorServices {
    Instructor getInstructorById(Long id);
    List<Instructor> getAllInstructors();
    void updateInstructor(Instructor instructor, Long id);
    void deleteInstructor(Long id);
    Instructor addInstructor(Instructor instructor);
    Map<String,String> getInstructorInformationById(Long id);
    Long getInstructorIdFromPrincipal(Principal principal);
    List<HashMap<String,String>> getInstructorsWithDetails();
    HashMap<String,String> getInstructorWithDetails(Long id);

}
