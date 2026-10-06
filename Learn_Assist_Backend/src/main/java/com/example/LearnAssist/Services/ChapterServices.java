package com.example.LearnAssist.Services;

import com.example.LearnAssist.Models.Chapter;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface ChapterServices {
    Chapter getChapter(Long id, Principal principal);
    List<Chapter> getAllChapters();
    void addChapter(Chapter chapter, MultipartFile video, MultipartFile document, String email);
    void editChapter(Long id, Chapter chapter, MultipartFile video, MultipartFile document, String email);
    void deleteChapter(Long id, String email);
    Integer getInstructorTotalVideosChapters(Principal principal);
    Integer getInstructorTotalResourcesChapters(Principal principal);

}
