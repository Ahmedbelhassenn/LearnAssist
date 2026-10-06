package com.example.LearnAssist.Services;

import com.example.LearnAssist.Storage.FileCategory;
import com.example.LearnAssist.Storage.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileStorageService {

    /**
     * Validates the upload (size, extension, MIME type, content signature) and stores it
     * under a server-generated name (UUID + extension).
     *
     * @return the stored file name, to be saved on the entity
     * @throws com.example.LearnAssist.Exceptions.InvalidFileException if the file is rejected
     */
    String store(MultipartFile file, FileCategory category);

    /** Same as {@link #store} but returns null when no file was sent. */
    String storeIfPresent(MultipartFile file, FileCategory category);

    /** Returns the file if the name is safe and the file exists, empty otherwise. */
    Optional<StoredFile> load(FileCategory category, String fileName);

    /**
     * Deletes a file that is no longer referenced. If a transaction is active the deletion
     * is deferred until it commits, so a rollback never leaves an entity pointing to a
     * deleted file. Failures are logged, never thrown.
     */
    void delete(FileCategory category, String fileName);
}
