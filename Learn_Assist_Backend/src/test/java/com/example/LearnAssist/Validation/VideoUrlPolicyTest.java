package com.example.LearnAssist.Validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Regression tests for audit finding F-01 (backend side): only allow-listed HTTPS embeds. */
class VideoUrlPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ?start=10",
            "https://player.vimeo.com/video/76979871"
    })
    void acceptsAllowListedEmbeds(String url) {
        assertThat(VideoUrlPolicy.isAllowedEmbedUrl(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(document.domain)",
            "JavaScript:alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "file:///etc/passwd",
            "http://www.youtube.com/embed/abc",
            "https://evil.com/embed/abc",
            "https://www.youtube.com.evil.com/embed/abc",
            "https://evil.com@www.youtube.com/embed/abc",
            "https://www.youtube.com/watch?v=abc",
            "https://www.youtube.com/embed/",
            "https://www.youtube.com:8443/embed/abc",
            "//www.youtube.com/embed/abc",
            "not a url"
    })
    void rejectsEverythingElse(String url) {
        assertThat(VideoUrlPolicy.isAllowedEmbedUrl(url)).isFalse();
    }
}
