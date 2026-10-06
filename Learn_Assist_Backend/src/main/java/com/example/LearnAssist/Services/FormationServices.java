package com.example.LearnAssist.Services;


import com.example.LearnAssist.Dto.CreateFormationRequest;
import com.example.LearnAssist.Dto.UpdateFormationRequest;
import com.example.LearnAssist.Models.Formation;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

public interface FormationServices {
    List<Formation> getAllFormations();
    Long addFormation(CreateFormationRequest request, MultipartFile image, MultipartFile video, String email);
    void updateFormation(Long id, UpdateFormationRequest request, MultipartFile image, MultipartFile video, String email);
    void deleteFormation(Long id, String email);
    List<HashMap<String,String>> getFormationsCard();
    List<HashMap<String,String>> getFormationsCardByEmail(String email);
    HashMap<String,String> getFormationCardById(Long id);
    void updateFormationStatus(Long id, String status, String email);
    List<HashMap<String,String>> getFormationsCardByInstructorEmail(String email);
    Integer getInstructorTotalActiveFormation(Principal principal);
    Integer getInstructorTotalVideosFormation(Principal principal);

}
