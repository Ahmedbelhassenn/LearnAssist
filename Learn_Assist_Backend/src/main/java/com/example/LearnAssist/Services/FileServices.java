package com.example.LearnAssist.Services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileServices {
    String saveVideoFile(MultipartFile file) throws IOException;
    String saveDocumentFile(MultipartFile file) throws IOException;
}
