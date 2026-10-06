package com.example.LearnAssist.Authentification;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Dto.RegisterInstructorRequest;
import com.example.LearnAssist.Jwt.JwtUtils;
import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Services.InstructorServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("api/instructor")
public class InstructorAuthenticationController {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private InstructorServices instructorServices;
    @Autowired
    private InstructorRepository instructorRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        if(!instructorRepository.existsByEmail(loginRequest.getEmail())){
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            map.put("error", "User not found");
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "User is not authenticated");
            map.put("status", false);
            map.put("error", exception.getMessage());
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Instructor instructor=instructorRepository.findByEmail(loginRequest.getEmail()).get();
        Long id=instructor.getId();
        String photoName=instructor.getProfilePhoto();
        String gender=instructor.getGender();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        LoginResponse response = new LoginResponse(userDetails.getUsername(), roles, jwtToken,id, photoName, gender);
        return ResponseEntity.ok(response);
    }
    /**
     * The body is bound to a DTO without id/role/status, so a client can never target or
     * overwrite an existing account: a new instructor is always created.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerInstructor(@Valid @RequestBody RegisterInstructorRequest request) {
        Instructor registeredInstructor;
        try {
            registeredInstructor = instructorServices.registerInstructor(request);
        }
        catch (ExceptionError e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        LoginRequest loginRequest= new LoginRequest(registeredInstructor.getEmail(), request.getPassword());
        return authenticateUser(loginRequest);
    }

}
