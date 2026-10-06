package com.example.LearnAssist.Controllers;



import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Dto.CreateFormationRequest;
import com.example.LearnAssist.Dto.UpdateFormationRequest;
import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Services.FormationServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/formations")
public class FormationController {
    @Autowired
    FormationServices formationServices;


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
    public ResponseEntity<?> updateFormation(Principal principal, @PathVariable Long id,
                                             @Valid @RequestBody UpdateFormationRequest formation) {
        HashMap<String,String> response = new HashMap<>();
        try{
            String email = principal.getName();

            formationServices.updateFormation(id, formation, null, null, email);
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
    public ResponseEntity<?> addFormation(Principal principal,
                                          @Valid @RequestPart("formation") CreateFormationRequest formation,
                                          @RequestPart(value = "image", required = false) MultipartFile image,
                                          @RequestPart(value = "video", required = false) MultipartFile video) {
        HashMap<String,String> response = new HashMap<>();
        try {
            // The owner is always the authenticated instructor.
            Long id = formationServices.addFormation(formation, image, video, principal.getName());
            response.put("message", "Formation added successfully");
            response.put("id", String.valueOf(id));
            return ResponseEntity.ok(response);
        }catch (ExceptionError e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> editFormation(Principal principal,
                                           @Valid @RequestPart("formation") UpdateFormationRequest formation,
                                           @RequestPart(value = "image", required = false) MultipartFile image,
                                           @RequestPart(value = "video", required = false) MultipartFile video,
                                           @PathVariable Long id) {
        HashMap<String,String> response = new HashMap<>();
        try {
            formationServices.updateFormation(id, formation, image, video, principal.getName());
            response.put("message", "Formation updated successfully");
            return ResponseEntity.ok(response);
        }catch (ExceptionError e) {
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
