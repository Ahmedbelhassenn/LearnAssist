package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Configurations.SecurityConfig;
import com.example.LearnAssist.Jwt.AuthEntryPointJwt;
import com.example.LearnAssist.Jwt.JwtUtils;
import com.example.LearnAssist.Repositories.AdminRepository;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Base for @WebMvcTest slices that run with the application's real SecurityConfig
 * (URL rules, @PreAuthorize, 401 entry point). Persistence and JWT parsing are mocked.
 */
@Import({SecurityConfig.class, AuthEntryPointJwt.class})
abstract class SecuredWebMvcTestSupport {
    @MockitoBean
    ParticipantRepository participantRepository;
    @MockitoBean
    InstructorRepository instructorRepository;
    @MockitoBean
    AdminRepository adminRepository;
    @MockitoBean
    JwtUtils jwtUtils;
}
