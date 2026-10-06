package com.example.LearnAssist.Models;

import com.example.LearnAssist.Dto.InstructorResponse;
import com.example.LearnAssist.Dto.ParticipantResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Regression tests for audit finding B-08 (password hash serialised in JSON). */
class PasswordSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void entitiesNeverSerializeThePassword() throws Exception {
        Participant participant = new Participant();
        participant.setEmail("a@x.com");
        participant.setPassword("$2a$10$hash");
        Instructor instructor = new Instructor();
        instructor.setEmail("p@x.com");
        instructor.setPassword("$2a$10$hash");

        assertThat(mapper.writeValueAsString(participant)).doesNotContain("password").doesNotContain("$2a$");
        assertThat(mapper.writeValueAsString(instructor)).doesNotContain("password").doesNotContain("$2a$");
    }

    @Test
    void responseDtosHaveNoPasswordField() throws Exception {
        Participant participant = new Participant();
        participant.setPassword("$2a$10$hash");
        Instructor instructor = new Instructor();
        instructor.setPassword("$2a$10$hash");

        assertThat(mapper.writeValueAsString(ParticipantResponse.from(participant))).doesNotContain("password");
        assertThat(mapper.writeValueAsString(InstructorResponse.from(instructor))).doesNotContain("password");
    }

    @Test
    void passwordCanStillBeReadFromARequestBody() throws Exception {
        Participant participant = mapper.readValue("{\"password\":\"secret123\"}", Participant.class);

        assertThat(participant.getPassword()).isEqualTo("secret123");
    }
}
