package com.example.LearnAssist.Controllers;



import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Services.FileServices;
import com.example.LearnAssist.Services.FormationServices;
import com.example.LearnAssist.ServicesImplementations.ProfilePictureServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/formations")
public class FormationController {
    @Autowired
    FormationServices formationServices;

    @Autowired
    ProfilePictureServicesImpl profilePictureServices;

    @Autowired
    FileServices fileServices;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Formation> getAllFormations() {
        return formationServices.getAllFormations();
    }

    @PreAuthorize("hasAnyRole('PARTICIPANT','ADMIN','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getFormationById(@PathVariable Long id) {
        HashMap<String,String> hashMap = new HashMap<>();
        try {
            hashMap=formationServices.getFormationCardById(id);
            return ResponseEntity.ok(hashMap);
        }
        catch (Exception e) {
            hashMap.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(hashMap);
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("update/{id}")
    public ResponseEntity<?> updateFormation(Principal principal, @PathVariable Long id, @RequestBody Formation formation) {
        HashMap<String,String> response = new HashMap<>();
        try{
            String email = principal.getName();

            formationServices.updateFormation(id, formation, email);
            response.put("message", "Formation updated");
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateFormationStatus(@PathVariable Long id, @RequestBody String status, Principal principal) {
        HashMap<String,String> response = new HashMap<>();
        try{
            String email = principal.getName();
            formationServices.updateFormationStatus(id, status, email);
            response.put("message", "Formation updated");
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<?> addFormation(Principal principal, @RequestPart Formation formation, @RequestParam Map<String, MultipartFile> files) {
        HashMap<String,String> response = new HashMap<>();
        try {
            String imageName = profilePictureServices.saveImage(files.get("image"));
            formation.setImageFileName(imageName);
            String videoFileName = fileServices.saveVideoFile(files.get("video"));
            formation.setVideoFileName(videoFileName);
            String email= principal.getName();
            Long id= formationServices.addFormation(formation,email);
            response.put("message", "Formation added successfully");
            response.put("id", String.valueOf(id));
            return ResponseEntity.ok(response);
        }catch (ExceptionError | IOException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> editFormation(Principal principal, @RequestPart Formation formation, @RequestParam Map<String, MultipartFile> files,
                                           @PathVariable Long id) {
        HashMap<String,String> response = new HashMap<>();
        try {
            MultipartFile image=files.get("image");
            if(image != null && !image.isEmpty()) {
                String fileName = profilePictureServices.saveImage(files.get("image"));
                formation.setImageFileName(fileName);
            }
            MultipartFile video=files.get("video");
            if(video != null && !video.isEmpty()) {
                String fileName = fileServices.saveVideoFile(files.get("video"));
                formation.setVideoFileName(fileName);
            }
            String email = principal.getName();
            formationServices.updateFormation(id, formation, email);
            response.put("message", "Formation updated successfully");
            return ResponseEntity.ok(response);
        }catch (ExceptionError | IOException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFormation(@PathVariable Long id, Principal principal) {
        HashMap<String,String> response = new HashMap<>();
        try{
            String email = principal.getName();
            formationServices.deleteFormation(id, email);
            response.put("message", "Formation deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

    }

    @GetMapping("/card")
    public ResponseEntity<?> getFormationsCard(){
        try {
            List<HashMap<String,String>> formations = formationServices.getFormationsCard();
            return ResponseEntity.ok(formations);
        }catch (ExceptionError e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/instructor/card")
    public ResponseEntity<?> getFormationsCardInstructor(Principal principal){
        try {
            String email = principal.getName();
            List<HashMap<String,String>> formations=formationServices.getFormationsCardByEmail(email);
            if(formations.isEmpty()){
                return ResponseEntity.badRequest().body("No formation found for instructor");
            }
            return ResponseEntity.ok(formations);
        }catch (ExceptionError e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/participant/{email}/card")
    public ResponseEntity<?> getFormationsCardByInstructorEmail(@PathVariable String email){
        HashMap<String,Object> response = new HashMap<>();
        try{
            List<HashMap<String,String>> formations=formationServices.getFormationsCardByInstructorEmail(email);
            response.put("information", formations);
            return ResponseEntity.ok(response);

        }catch (ExceptionError e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @GetMapping("/total/active")
    public ResponseEntity<?> getFormationsTotalActive(Principal principal){
        HashMap<String,Object> response = new HashMap<>();
        try {
            int result = formationServices.getInstructorTotalActiveFormation(principal);
            response.put("result", result);
            return ResponseEntity.ok(response);
        }catch (ExceptionError e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }




}
