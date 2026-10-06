package com.example.LearnAssist.Validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;

/**
 * Allow-list of external video providers that may be embedded in an iframe.
 * Keep it in sync with the frontend pipe {@code safe-embed-url.pipe.ts}.
 */
public final class VideoUrlPolicy {

    /** host -> required path prefix */
    private static final Map<String, String> ALLOWED_EMBEDS = Map.of(
            "www.youtube.com", "/embed/",
            "youtube.com", "/embed/",
            "www.youtube-nocookie.com", "/embed/",
            "player.vimeo.com", "/video/"
    );

    private VideoUrlPolicy() {
    }

    public static boolean isAllowedEmbedUrl(String value) {
        if (value == null || value.length() > 500) {
            return false;
        }
        try {
            URI uri = new URI(value.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getRawUserInfo() != null
                    || uri.getHost() == null || (uri.getPort() != -1 && uri.getPort() != 443)) {
                return false;
            }
            String requiredPrefix = ALLOWED_EMBEDS.get(uri.getHost().toLowerCase(Locale.ROOT));
            String path = uri.getPath();
            return requiredPrefix != null && path != null && path.startsWith(requiredPrefix)
                    && path.length() > requiredPrefix.length() && !path.contains("..");
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
