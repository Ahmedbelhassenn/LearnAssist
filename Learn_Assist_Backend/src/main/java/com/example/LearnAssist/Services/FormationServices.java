package com.example.LearnAssist.Services;


import com.example.LearnAssist.Models.Formation;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

public interface FormationServices {
    List<Formation> getAllFormations();
    Long addFormation(Formation formation, String email);
    void updateFormation(Long id, Formation formation, String email);
    void deleteFormation(Long id, String email);
    List<HashMap<String,String>> getFormationsCard();
    List<HashMap<String,String>> getFormationsCardByEmail(String email);
    HashMap<String,String> getFormationCardById(Long id);
    void updateFormationStatus(Long id, String status, String email);
    List<HashMap<String,String>> getFormationsCardByInstructorEmail(String email);
    Integer getInstructorTotalActiveFormation(Principal principal);
    Integer getInstructorTotalVideosFormation(Principal principal);

}
