package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Exceptions.InvalidFileException;
import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.Storage.FileCategory;
import com.example.LearnAssist.Storage.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private static final int SIGNATURE_LENGTH = 16;

    private final Path rootDirectory;

    public FileStorageServiceImpl(@Value("${app.upload.base-dir:uploads}") String baseDir) {
        this.rootDirectory = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        if (file.getSize() > category.getMaxSizeBytes()) {
            throw new InvalidFileException("File is too large (max "
                    + category.getMaxSizeBytes() / (1024 * 1024) + " MB)");
        }

        // The original name is only used to read the extension, never to build the path.
        String extension = extractExtension(file.getOriginalFilename());
        Set<String> allowedMimeTypes = category.getAllowedTypes().get(extension);
        if (allowedMimeTypes == null) {
            throw new InvalidFileException("File type not allowed. Allowed: "
                    + String.join(", ", category.getAllowedTypes().keySet()));
        }
        String announcedType = file.getContentType();
        if (StringUtils.hasText(announcedType)
                && !MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(announcedType)
                && !allowedMimeTypes.contains(announcedType.toLowerCase(Locale.ROOT))) {
            throw new InvalidFileException("File content type does not match its extension");
        }
        if (!hasExpectedSignature(file, extension)) {
            throw new InvalidFileException("File content does not match its extension");
        }

        Path directory = categoryDirectory(category);
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = directory.resolve(fileName).normalize();
        if (!target.getParent().equals(directory)) {
            throw new InvalidFileException("Invalid file path");
        }
        try {
            Files.createDirectories(directory);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target); // no REPLACE_EXISTING: never overwrite an existing file
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store file", e);
        }
        return fileName;
    }

    @Override
    public String storeIfPresent(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return store(file, category);
    }

    @Override
    public Optional<StoredFile> load(FileCategory category, String fileName) {
        return resolveExisting(category, fileName).map(path -> new StoredFile(
                new PathResource(path),
                MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM),
                fileName));
    }

    @Override
    public void delete(FileCategory category, String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteNow(category, fileName);
                }
            });
        } else {
            deleteNow(category, fileName);
        }
    }

    private void deleteNow(FileCategory category, String fileName) {
        resolveExisting(category, fileName).ifPresent(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                logger.warn("Could not delete file {} in {}", fileName, category.getDirectory());
            }
        });
    }

    /**
     * Resolves a file name inside the category folder. Accepts the legacy names
     * ("uuid_original name.png") but refuses anything that would leave the folder.
     */
    private Optional<Path> resolveExisting(FileCategory category, String fileName) {
        if (!StringUtils.hasText(fileName) || fileName.contains("/") || fileName.contains("\\")
                || fileName.contains(":") || fileName.indexOf('\0') >= 0) {
            return Optional.empty();
        }
        Path directory = categoryDirectory(category);
        Path target;
        try {
            target = directory.resolve(fileName).normalize();
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
        if (!target.startsWith(directory) || !directory.equals(target.getParent())) {
            return Optional.empty();
        }
        if (!Files.isRegularFile(target) || !Files.isReadable(target)) {
            return Optional.empty();
        }
        return Optional.of(target);
    }

    private Path categoryDirectory(FileCategory category) {
        return rootDirectory.resolve(category.getDirectory()).normalize();
    }

    private static String extractExtension(String originalName) {
        String extension = StringUtils.getFilenameExtension(originalName);
        if (!StringUtils.hasText(extension)) {
            throw new InvalidFileException("File has no extension");
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    /** Checks the first bytes ("magic numbers") so a renamed file is rejected. */
    private static boolean hasExpectedSignature(MultipartFile file, String extension) {
        byte[] header;
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(SIGNATURE_LENGTH);
        } catch (IOException e) {
            return false;
        }
        return switch (extension) {
            case "jpg", "jpeg" -> startsWith(header, 0, (byte) 0xFF, (byte) 0xD8, (byte) 0xFF);
            case "png" -> startsWith(header, 0, (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
            case "gif" -> ascii(header, 0, "GIF87a") || ascii(header, 0, "GIF89a");
            case "webp" -> ascii(header, 0, "RIFF") && ascii(header, 8, "WEBP");
            case "pdf" -> ascii(header, 0, "%PDF-");
            case "doc" -> startsWith(header, 0, (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                    (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1);
            case "docx" -> startsWith(header, 0, 'P', 'K', 0x03, 0x04);
            case "mp4", "m4v" -> ascii(header, 4, "ftyp");
            case "mov" -> ascii(header, 4, "ftyp") || ascii(header, 4, "moov")
                    || ascii(header, 4, "mdat") || ascii(header, 4, "wide") || ascii(header, 4, "free");
            case "webm" -> startsWith(header, 0, 0x1A, 0x45, (byte) 0xDF, (byte) 0xA3);
            case "ogg", "ogv" -> ascii(header, 0, "OggS");
            default -> false;
        };
    }

    private static boolean ascii(byte[] header, int offset, String expected) {
        byte[] bytes = expected.getBytes(StandardCharsets.US_ASCII);
        return header.length >= offset + bytes.length
                && Arrays.equals(header, offset, offset + bytes.length, bytes, 0, bytes.length);
    }

    private static boolean startsWith(byte[] header, int offset, int... expected) {
        if (header.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (header[offset + i] != (byte) expected[i]) {
                return false;
            }
        }
        return true;
    }
}
