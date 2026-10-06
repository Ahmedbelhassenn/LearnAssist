package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Dto.RegisterInstructorRequest;
import com.example.LearnAssist.Dto.RegisterParticipantRequest;
import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Regression tests for audit finding B-01 (account takeover through mass assignment). */
@ExtendWith(MockitoExtension.class)
class RegistrationServicesTest {

    @Mock
    ParticipantRepository participantRepository;
    @Mock
    InstructorRepository instructorRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EmailAvailabilityChecker emailAvailabilityChecker;

    @InjectMocks
    ParticipantServicesImpl participantServices;
    @InjectMocks
    InstructorServicesImpl instructorServices;

    @Test
    void participantRegistrationAlwaysCreatesANewEntity() {
        when(emailAvailabilityChecker.isTaken("new@x.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterParticipantRequest request = new RegisterParticipantRequest();
        request.setFirstName("A");
        request.setLastName("B");
        request.setPhone("+216 12345678");
        request.setEmail(" new@x.com ");
        request.setPassword("Password1");
        request.setCity("Tunis");

        participantServices.registerParticipant(request);

        ArgumentCaptor<Participant> saved = ArgumentCaptor.forClass(Participant.class);
        verify(participantRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).as("id must be generated, never client-supplied").isNull();
        assertThat(saved.getValue().getRole()).isEqualTo("PARTICIPANT");
        assertThat(saved.getValue().getStatus()).isNull();
        assertThat(saved.getValue().getEmail()).isEqualTo("new@x.com");
        assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
    }

    @Test
    void participantRegistrationRejectsAnEmailUsedByAnyAccount() {
        when(emailAvailabilityChecker.isTaken("Taken@X.com")).thenReturn(true);
        RegisterParticipantRequest request = new RegisterParticipantRequest();
        request.setEmail("Taken@X.com");

        assertThatThrownBy(() -> participantServices.registerParticipant(request))
                .isInstanceOf(ExceptionError.class)
                .hasMessage("Email already registered!");
        verify(participantRepository, never()).save(any());
    }

    @Test
    void instructorRegistrationAlwaysCreatesANewEntity() {
        when(emailAvailabilityChecker.isTaken("prof@x.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(instructorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterInstructorRequest request = new RegisterInstructorRequest();
        request.setFirstName("A");
        request.setLastName("B");
        request.setPhone("+216 12345678");
        request.setEmail("prof@x.com");
        request.setPassword("Password1");
        request.setCity("Tunis");
        request.setSpeciality("Java");

        instructorServices.registerInstructor(request);

        ArgumentCaptor<Instructor> saved = ArgumentCaptor.forClass(Instructor.class);
        verify(instructorRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isNull();
        assertThat(saved.getValue().getRole()).isEqualTo("INSTRUCTOR");
        assertThat(saved.getValue().getStatus()).isNull();
        assertThat(saved.getValue().getSpeciality()).isEqualTo("Java");
    }
}
