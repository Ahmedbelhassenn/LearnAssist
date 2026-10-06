package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Exceptions.ResourceNotFoundException;
import com.example.LearnAssist.Models.ChatSession;
import com.example.LearnAssist.Repositories.ChatMessageRepository;
import com.example.LearnAssist.Repositories.ChatSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Regression tests for audit finding B-03 (chat sessions readable by other participants). */
@ExtendWith(MockitoExtension.class)
class ChatSessionServicesImplTest {

    @Mock
    ChatSessionRepository chatSessionRepository;
    @Mock
    ChatMessageRepository chatMessageRepository;

    @InjectMocks
    ChatSessionServicesImpl chatSessionServices;

    @Test
    void participantCannotReadAnotherParticipantsSession() {
        when(chatSessionRepository.findByIdAndParticipantEmail(5L, "b@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatSessionServices.getMessagesBySessionId(5L, "b@x.com"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(chatSessionRepository, never()).findById(anyLong());
        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    void ownerCanReadItsSession() {
        when(chatSessionRepository.findByIdAndParticipantEmail(5L, "a@x.com"))
                .thenReturn(Optional.of(new ChatSession()));
        when(chatMessageRepository.findByChatSessionIdOrderByIdAsc(5L)).thenReturn(List.of());

        assertThat(chatSessionServices.getMessagesBySessionId(5L, "a@x.com")).isEmpty();
    }

    @Test
    void participantCannotDeleteOrRenameAnotherParticipantsSession() {
        when(chatSessionRepository.findByIdAndParticipantEmail(5L, "b@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatSessionServices.deleteSession(5L, "b@x.com"))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> chatSessionServices.editSessionTitle(5L, "pwned", "b@x.com"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(chatSessionRepository, never()).delete(any());
        verify(chatSessionRepository, never()).save(any());
    }

    @Test
    void sessionListIsQueriedForTheCallerOnly() {
        chatSessionServices.getSessionsByParticipantEmail("a@x.com");

        verify(chatSessionRepository).findByParticipantEmailOrderByCreatedAtDesc("a@x.com");
        verify(chatSessionRepository, never()).findAll();
    }
}
