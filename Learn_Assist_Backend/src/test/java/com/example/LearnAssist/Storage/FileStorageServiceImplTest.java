package com.example.LearnAssist.Storage;

import com.example.LearnAssist.Exceptions.InvalidFileException;
import com.example.LearnAssist.ServicesImplementations.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regression tests for audit finding B-06 (unsafe uploads). */
class FileStorageServiceImplTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] PDF = "%PDF-1.7 test".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] MP4 = {0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};

    @TempDir
    Path root;

    private FileStorageServiceImpl storage;

    @BeforeEach
    void setUp() {
        storage = new FileStorageServiceImpl(root.toString());
    }

    @Test
    void storesValidImageUnderServerGeneratedName() throws Exception {
        String name = storage.store(new MockMultipartFile("file", "photo.PNG", "image/png", PNG),
                FileCategory.PROFILE_PICTURE);

        assertThat(name).matches("[0-9a-f-]{36}\\.png");
        assertThat(Files.readAllBytes(root.resolve("profile-pictures").resolve(name))).isEqualTo(PNG);
    }

    @Test
    void originalFilenameWithTraversalNeverControlsThePath() throws Exception {
        String name = storage.store(new MockMultipartFile("file", "..\\..\\..\\evil.pdf", "application/pdf", PDF),
                FileCategory.DOCUMENT);

        assertThat(name).doesNotContain("..").doesNotContain("\\").doesNotContain("/").endsWith(".pdf");
        try (var files = Files.list(root.resolve("documents"))) {
            assertThat(files).containsExactly(root.resolve("documents").resolve(name));
        }
    }

    @Test
    void rejectsForbiddenExtension() {
        MockMultipartFile html = new MockMultipartFile("file", "page.html", "text/html",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> storage.store(html, FileCategory.ARTICLE_IMAGE))
                .isInstanceOf(InvalidFileException.class);
        assertThat(root.resolve("article-pictures")).doesNotExist();
    }

    @Test
    void rejectsFileWhoseContentDoesNotMatchItsExtension() {
        MockMultipartFile renamedScript = new MockMultipartFile("file", "image.png", "image/png",
                "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> storage.store(renamedScript, FileCategory.FORMATION_IMAGE))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("content");
    }

    @Test
    void rejectsMismatchingAnnouncedMimeType() {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "text/html", MP4);

        assertThatThrownBy(() -> storage.store(file, FileCategory.VIDEO))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void acceptsGenericOctetStreamWhenContentIsValid() {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, MP4);

        assertThat(storage.store(file, FileCategory.VIDEO)).endsWith(".mp4");
    }

    @Test
    void rejectsTooLargeFile() {
        byte[] big = new byte[(int) FileCategory.PROFILE_PICTURE.getMaxSizeBytes() + 1];
        System.arraycopy(PNG, 0, big, 0, PNG.length);

        assertThatThrownBy(() -> storage.store(new MockMultipartFile("file", "big.png", "image/png", big),
                FileCategory.PROFILE_PICTURE))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("too large");
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> storage.store(new MockMultipartFile("file", "a.png", "image/png", new byte[0]),
                FileCategory.PROFILE_PICTURE))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void loadRefusesNamesThatLeaveTheCategoryFolder() throws Exception {
        Files.createDirectories(root.resolve("documents"));
        Files.writeString(root.resolve("secret.txt"), "secret");

        assertThat(storage.load(FileCategory.DOCUMENT, "../secret.txt")).isEmpty();
        assertThat(storage.load(FileCategory.DOCUMENT, "..\\secret.txt")).isEmpty();
        assertThat(storage.load(FileCategory.DOCUMENT, "..")).isEmpty();
        assertThat(storage.load(FileCategory.DOCUMENT, "C:secret.txt")).isEmpty();
    }

    @Test
    void loadStillServesLegacyFileNames() throws Exception {
        Path dir = Files.createDirectories(root.resolve("courses-pictures"));
        Files.write(dir.resolve("1b2c_my photo.png"), PNG);

        assertThat(storage.load(FileCategory.FORMATION_IMAGE, "1b2c_my photo.png"))
                .hasValueSatisfying(file -> assertThat(file.mediaType()).isEqualTo(MediaType.IMAGE_PNG));
    }

    @Test
    void deleteRemovesFileWithoutTransaction() throws Exception {
        String name = storage.store(new MockMultipartFile("file", "a.png", "image/png", PNG),
                FileCategory.PROFILE_PICTURE);

        storage.delete(FileCategory.PROFILE_PICTURE, name);

        assertThat(root.resolve("profile-pictures").resolve(name)).doesNotExist();
    }
}
