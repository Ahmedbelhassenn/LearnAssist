package com.example.LearnAssist.Dto;

import com.example.LearnAssist.Validation.AllowedVideoUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Fields an instructor may change on an existing formation. Null fields are left unchanged.
 * Not accepted: id, emailInstructor, rate, imageFileName / videoFileName (set from uploads only).
 * The publication status is changed through PATCH /api/formations/{id}.
 */
@Data
public class UpdateFormationRequest {
    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 2000)
    private String description;

    @AllowedVideoUrl
    @Size(max = 500)
    private String videoUrl;

    @Size(max = 255)
    private String formationCategory;

    @Size(max = 255)
    private String formationLanguage;

    @Size(max = 255)
    private String price;

    @Size(max = 255)
    private String formationDuration;

    @Size(max = 255)
    private String formationLevel;

    @Size(max = 255)
    private String certification;
}
