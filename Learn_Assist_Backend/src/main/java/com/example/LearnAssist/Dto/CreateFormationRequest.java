package com.example.LearnAssist.Dto;

import com.example.LearnAssist.Validation.AllowedVideoUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Fields an instructor may set when creating a formation.
 * Deliberately NOT accepted from the client: id, emailInstructor (owner), rate,
 * formationStatus (always "draft" at creation), imageFileName / videoFileName (set from uploads).
 * Unknown JSON properties are ignored, so old clients sending them keep working.
 */
@Data
public class CreateFormationRequest {
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
