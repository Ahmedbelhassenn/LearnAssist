package com.example.LearnAssist.Controllers;


import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Services.InstructorServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@CrossOrigin("http://localhost:4200")
public class InstructorController {
    @Autowired
    private InstructorServices instructorServices;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Instructor getInstructorById(@PathVariable Long id){
        return instructorServices.getInstructorById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Instructor> getAllInstructors(){
        return instructorServices.getAllInstructors();
    }


    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PatchMapping("/{oldPassword}")
    public ResponseEntity<?> updateInstructor(Principal principal,
                                              @PathVariable String oldPassword, @RequestBody Instructor instructor){
        try {
            Long id = instructorServices.getInstructorIdFromPrincipal(principal);
            Instructor instructor1=instructorServices.getInstructorById(id);
            HashMap<String,String> hashMap=new HashMap<>();
            if(!passwordEncoder.matches(oldPassword,instructor1.getPassword())){
                hashMap.put("error","Wrong old password");
                return ResponseEntity.badRequest().body(hashMap);
            }
            instructorServices.updateInstructor(instructor, id);
            hashMap.put("Succèss", "Instructor successfully updated");
            return ResponseEntity.ok(hashMap);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping()
    public ResponseEntity<?> updateInstructor(Principal principal, @RequestBody Instructor instructor){
        try {
            Long id = instructorServices.getInstructorIdFromPrincipal(principal);
            instructorServices.updateInstructor(instructor, id);
            HashMap<String,String> response = new HashMap<>();
            response.put("message", "Instructor updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteInstructor(@PathVariable Long id){
        instructorServices.deleteInstructor(id);
    }


    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping ("information")
    public ResponseEntity<?> getInstructorInformation(Principal principal) {
        try {
            Long id = instructorServices.getInstructorIdFromPrincipal(principal);
            return ResponseEntity.ok().body(instructorServices.getInstructorInformationById(id));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getInstructorsWithDetails(){
        HashMap<String,Object> hashMap=new HashMap<>();
        try {
            hashMap.put("list",instructorServices.getInstructorsWithDetails());
            return ResponseEntity.ok(hashMap);
        }catch (Exception e){
            hashMap.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(hashMap);
        }
    }



    @GetMapping("/details/{id}")
    public ResponseEntity<?> getInstructorDetailsById(@PathVariable Long id){
        HashMap<String,Object> hashMap=new HashMap<>();
        try {
            HashMap<String,String> hashMap1=instructorServices.getInstructorWithDetails(id);
            hashMap.put("details",hashMap1);
            return ResponseEntity.ok(hashMap);
        }catch (Exception e){
            hashMap.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(hashMap);
        }
    }



}
