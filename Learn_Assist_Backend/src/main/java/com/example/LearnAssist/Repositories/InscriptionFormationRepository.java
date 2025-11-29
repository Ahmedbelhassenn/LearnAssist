package com.example.LearnAssist.Repositories;

import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Models.InscriptionFormation;
import com.example.LearnAssist.Models.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InscriptionFormationRepository extends JpaRepository<InscriptionFormation, Long> {
    Optional<InscriptionFormation> findInscriptionFormationByFormationAndParticipant(Formation formation, Participant participant);
}
