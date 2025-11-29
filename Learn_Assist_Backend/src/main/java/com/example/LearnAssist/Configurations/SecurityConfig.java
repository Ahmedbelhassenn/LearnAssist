package com.example.LearnAssist.Configurations;

import com.example.LearnAssist.Jwt.AuthEntryPointJwt;
import com.example.LearnAssist.Jwt.AuthTokenFilter;
import com.example.LearnAssist.Models.Admin;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;

import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Repositories.AdminRepository;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;


@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    InstructorRepository instructorRepository;
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthTokenFilter authTokenFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/api/instructor/login","/api/formations/card", "api/instructors/details/**" ,
                                        "/api/instructor/register","api/participant/login","api/chat","api/profiles/image/**",
                                        "api/participant/register", "/error", "/api/files/image/**", "api/instructors/list",
                                          "api/formations/participant/**", "api/articles", "api/files/article-image/**"   ).permitAll()
                                .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authTokenFilter,
                        UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            // Check if the user is an Instructor
            Optional<Instructor> instructorOpt = instructorRepository.findByEmail(email);
            if (instructorOpt.isPresent()) {
                Instructor instructor = instructorOpt.get();
                return User.builder()
                        .username(instructor.getEmail())
                        .password(instructor.getPassword())
                        .roles(instructor.getRole())
                        .build();
            }
            // Check if the user is a Participant
            Optional<Participant> participantOpt = participantRepository.findByEmail(email);
            if (participantOpt.isPresent()) {
                Participant participant = participantOpt.get();
                return User.builder()
                        .username(participant.getEmail())
                        .password(participant.getPassword()) // Spring will check this
                        .roles(participant.getRole()) // Role should be in uppercase
                        .build();
            }

            // Check if the user is an Admin
            Optional<Admin> adminOpt = adminRepository.findByEmail(email);
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                return User.builder()
                        .username(admin.getEmail())
                        .password(admin.getPassword())
                        .roles(admin.getRole())
                        .build();
            }
            // If no user found, throw an exception
            throw new ExceptionError("User not found");
        };
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }

}
