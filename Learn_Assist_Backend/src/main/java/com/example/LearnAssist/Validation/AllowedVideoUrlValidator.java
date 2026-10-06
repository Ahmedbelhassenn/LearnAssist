package com.example.LearnAssist.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedVideoUrlValidator implements ConstraintValidator<AllowedVideoUrl, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || VideoUrlPolicy.isAllowedEmbedUrl(value);
    }
}
