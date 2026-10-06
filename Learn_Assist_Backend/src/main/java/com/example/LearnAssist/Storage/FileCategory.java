package com.example.LearnAssist.Storage;

import java.util.Map;
import java.util.Set;

/**
 * Every kind of file the platform stores, with its folder, allowed extensions
 * (and the MIME types a browser may legitimately announce for them) and its max size.
 */
public enum FileCategory {

    VIDEO("videos", Map.of(
            "mp4", Set.of("video/mp4"),
            "m4v", Set.of("video/mp4", "video/x-m4v"),
            "mov", Set.of("video/quicktime"),
            "webm", Set.of("video/webm"),
            "ogv", Set.of("video/ogg"),
            "ogg", Set.of("video/ogg", "application/ogg")
    ), 500L * 1024 * 1024),

    DOCUMENT("documents", Map.of(
            "pdf", Set.of("application/pdf"),
            "doc", Set.of("application/msword"),
            "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ), 50L * 1024 * 1024),

    FORMATION_IMAGE("courses-pictures", Images.TYPES, Images.MAX_SIZE),
    ARTICLE_IMAGE("article-pictures", Images.TYPES, Images.MAX_SIZE),
    PROFILE_PICTURE("profile-pictures", Images.TYPES, Images.MAX_SIZE);

    private final String directory;
    private final Map<String, Set<String>> allowedTypes;
    private final long maxSizeBytes;

    FileCategory(String directory, Map<String, Set<String>> allowedTypes, long maxSizeBytes) {
        this.directory = directory;
        this.allowedTypes = allowedTypes;
        this.maxSizeBytes = maxSizeBytes;
    }

    public String getDirectory() {
        return directory;
    }

    public Map<String, Set<String>> getAllowedTypes() {
        return allowedTypes;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    private static final class Images {
        static final Map<String, Set<String>> TYPES = Map.of(
                "jpg", Set.of("image/jpeg", "image/pjpeg"),
                "jpeg", Set.of("image/jpeg", "image/pjpeg"),
                "png", Set.of("image/png"),
                "gif", Set.of("image/gif"),
                "webp", Set.of("image/webp")
        );
        static final long MAX_SIZE = 5L * 1024 * 1024;
    }
}
