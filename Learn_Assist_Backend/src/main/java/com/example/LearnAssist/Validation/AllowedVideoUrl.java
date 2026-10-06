package com.example.LearnAssist.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An external video URL must be an HTTPS embed URL from an allow-listed provider
 * (see {@link VideoUrlPolicy}). Null or blank values are valid (no external video).
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedVideoUrlValidator.class)
public @interface AllowedVideoUrl {
    String message() default "Video URL must be an HTTPS YouTube or Vimeo embed URL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
