package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Repositories.AdminRepository;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import org.springframework.stereotype.Component;

/**
 * Accounts live in three tables (participant, instructor, admin) and the login looks the
 * email up in all of them, so an email must be unique across all three, ignoring case.
 */
@Component
public class EmailAvailabilityChecker {
    private final ParticipantRepository participantRepository;
    private final InstructorRepository instructorRepository;
    private final AdminRepository adminRepository;

    public EmailAvailabilityChecker(ParticipantRepository participantRepository,
                                    InstructorRepository instructorRepository,
                                    AdminRepository adminRepository) {
        this.participantRepository = participantRepository;
        this.instructorRepository = instructorRepository;
        this.adminRepository = adminRepository;
    }

    public boolean isTaken(String email) {
        return participantRepository.existsByEmailIgnoreCase(email)
                || instructorRepository.existsByEmailIgnoreCase(email)
                || adminRepository.existsByEmailIgnoreCase(email);
    }
}
