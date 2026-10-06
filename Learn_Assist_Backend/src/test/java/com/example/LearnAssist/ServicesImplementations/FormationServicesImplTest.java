package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Dto.CreateFormationRequest;
import com.example.LearnAssist.Dto.UpdateFormationRequest;
import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Repositories.FormationRepository;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.Storage.FileCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Regression tests for audit finding B-02 (formation hijacking) and B-06 (upload ordering). */
@ExtendWith(MockitoExtension.class)
class FormationServicesImplTest {

    @Mock
    FormationRepository formationRepository;
    @Mock
    InstructorRepository instructorRepository;
    @Mock
    FileStorageService fileStorageService;

    @InjectMocks
    FormationServicesImpl formationServices;

    @Test
    void createdFormationIsOwnedByTheAuthenticatedInstructorAndStartsAsDraft() {
        when(instructorRepository.existsByEmail("owner@x.com")).thenReturn(true);
        when(formationRepository.save(any())).thenAnswer(inv -> {
            Formation f = inv.getArgument(0);
            f.setId(99L);
            return f;
        });
        CreateFormationRequest request = new CreateFormationRequest();
        request.setTitle("Spring");

        Long id = formationServices.addFormation(request, null, null, "owner@x.com");

        ArgumentCaptor<Formation> saved = ArgumentCaptor.forClass(Formation.class);
        verify(formationRepository).save(saved.capture());
        assertThat(id).isEqualTo(99L);
        assertThat(saved.getValue().getEmailInstructor()).isEqualTo("owner@x.com");
        assertThat(saved.getValue().getFormationStatus()).isEqualTo("draft");
        assertThat(saved.getValue().getRate()).isZero();
    }

    @Test
    void nonInstructorCannotCreateFormation() {
        when(instructorRepository.existsByEmail("participant@x.com")).thenReturn(false);
        CreateFormationRequest request = new CreateFormationRequest();
        request.setTitle("Spring");

        assertThatThrownBy(() -> formationServices.addFormation(request, null, null, "participant@x.com"))
                .isInstanceOf(ExceptionError.class);
        verify(formationRepository, never()).save(any());
    }

    @Test
    void instructorCannotModifyAnotherInstructorsFormationAndNoFileIsWritten() {
        Formation existing = new Formation();
        existing.setId(1L);
        existing.setTitle("Original");
        existing.setEmailInstructor("owner@x.com");
        when(formationRepository.findById(1L)).thenReturn(Optional.of(existing));
        UpdateFormationRequest request = new UpdateFormationRequest();
        request.setTitle("Hijacked");
        MockMultipartFile image = new MockMultipartFile("image", "a.png", "image/png", new byte[]{1});

        assertThatThrownBy(() -> formationServices.updateFormation(1L, request, image, null, "attacker@x.com"))
                .isInstanceOf(ExceptionError.class);

        verify(formationRepository, never()).save(any());
        verifyNoInteractions(fileStorageService);
        assertThat(existing.getTitle()).isEqualTo("Original");
    }

    @Test
    void replacingTheImageDeletesThePreviousFileAfterSaving() {
        Formation existing = new Formation();
        existing.setId(1L);
        existing.setTitle("T");
        existing.setEmailInstructor("owner@x.com");
        existing.setImageFileName("old.png");
        when(formationRepository.findById(1L)).thenReturn(Optional.of(existing));
        MockMultipartFile image = new MockMultipartFile("image", "a.png", "image/png", new byte[]{1});
        when(fileStorageService.storeIfPresent(image, FileCategory.FORMATION_IMAGE)).thenReturn("new.png");
        UpdateFormationRequest request = new UpdateFormationRequest();
        request.setTitle("T");

        formationServices.updateFormation(1L, request, image, null, "owner@x.com");

        assertThat(existing.getImageFileName()).isEqualTo("new.png");
        var order = inOrder(formationRepository, fileStorageService);
        order.verify(formationRepository).save(existing);
        order.verify(fileStorageService).delete(FileCategory.FORMATION_IMAGE, "old.png");
    }

    @Test
    void newlyStoredFilesAreRemovedWhenSavingFails() {
        when(instructorRepository.existsByEmail("owner@x.com")).thenReturn(true);
        MockMultipartFile image = new MockMultipartFile("image", "a.png", "image/png", new byte[]{1});
        when(fileStorageService.storeIfPresent(image, FileCategory.FORMATION_IMAGE)).thenReturn("new.png");
        when(formationRepository.save(any())).thenThrow(new IllegalStateException("db down"));
        CreateFormationRequest request = new CreateFormationRequest();
        request.setTitle("Spring");

        assertThatThrownBy(() -> formationServices.addFormation(request, image, null, "owner@x.com"))
                .isInstanceOf(IllegalStateException.class);
        verify(fileStorageService).delete(FileCategory.FORMATION_IMAGE, "new.png");
    }
}
