package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Article;
import com.example.LearnAssist.Models.Chapter;
import com.example.LearnAssist.Repositories.ArticleRepository;
import com.example.LearnAssist.Repositories.ChapterRepository;
import com.example.LearnAssist.Services.ChapterServices;
import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.Services.InscriptionFormationServices;
import com.example.LearnAssist.Storage.FileCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Service
public class ChapterServicesImpl implements ChapterServices {
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private InscriptionFormationServices inscriptionFormationServices;

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public Chapter getChapter(Long id, Principal principal) {
        Chapter chapter= chapterRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Chapter with id " + id + " not found")
        );
        Authentication authentication = (Authentication) principal ;
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_INSTRUCTOR")){
            return chapter;
        }
        Long idFormation= chapter.getCourse().getFormation().getId();
        if (inscriptionFormationServices.isParticipantApproved(idFormation, principal)) {
            return chapter;
        }
        else {
            throw (new ExceptionError("Chapter with id " + id + " is not approved"));
        }

    }

    @Override
    public List<Chapter> getAllChapters() {
        return chapterRepository.findAll();
    }

    @Override
    public void addChapter(Chapter chapter, MultipartFile video, MultipartFile document, String email) {
        String InstructorEmail = chapter.getCourse().
                getFormation().getEmailInstructor();
        if (!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to add this chapter");
        }
        if (chapterRepository.existsByTitle(chapter.getTitle())) {
            throw new ExceptionError("Chapter with this title already exists");
        }
        // Files are written only once ownership is confirmed; they are removed if the save fails.
        String videoFileName = null;
        String documentFileName = null;
        try {
            videoFileName = fileStorageService.storeIfPresent(video, FileCategory.VIDEO);
            documentFileName = fileStorageService.storeIfPresent(document, FileCategory.DOCUMENT);
            chapter.setVideoFileName(videoFileName);
            chapter.setDocumentFileName(documentFileName);
            chapterRepository.save(chapter);
        } catch (RuntimeException e) {
            fileStorageService.delete(FileCategory.VIDEO, videoFileName);
            fileStorageService.delete(FileCategory.DOCUMENT, documentFileName);
            throw e;
        }
    }

    @Override
    public void editChapter(Long id, Chapter chapter, MultipartFile video, MultipartFile document, String email) {
        Chapter existingChapter=chapterRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Chapter not found")
        );
        String InstructorEmail = existingChapter.getCourse().
                getFormation().getEmailInstructor();
        if (!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to edit this chapter");
        }
        if(!chapter.getTitle().equals(existingChapter.getTitle())) {
            if(chapterRepository.existsByTitle(chapter.getTitle())) {
                throw new ExceptionError("Chapter with this title already exists");
            }
        }
        existingChapter.setTitle(chapter.getTitle());
        if(chapter.getDescription()!=null){
            existingChapter.setDescription(chapter.getDescription());
        }
        if(chapter.getQuiz()!=null){
            existingChapter.setQuiz(chapter.getQuiz());
        }

        String previousVideo = existingChapter.getVideoFileName();
        String previousDocument = existingChapter.getDocumentFileName();
        String newVideo = null;
        String newDocument = null;
        try {
            newVideo = fileStorageService.storeIfPresent(video, FileCategory.VIDEO);
            newDocument = fileStorageService.storeIfPresent(document, FileCategory.DOCUMENT);
            if (newVideo != null) {
                existingChapter.setVideoFileName(newVideo);
            }
            if (newDocument != null) {
                existingChapter.setDocumentFileName(newDocument);
            }
            chapterRepository.save(existingChapter);
        } catch (RuntimeException e) {
            fileStorageService.delete(FileCategory.VIDEO, newVideo);
            fileStorageService.delete(FileCategory.DOCUMENT, newDocument);
            throw e;
        }
        // Replaced files are no longer referenced.
        if (newVideo != null) {
            fileStorageService.delete(FileCategory.VIDEO, previousVideo);
        }
        if (newDocument != null) {
            fileStorageService.delete(FileCategory.DOCUMENT, previousDocument);
        }
    }

    @Override
    public void deleteChapter(Long id, String email) {
        Chapter existingChapter=chapterRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Chapter not found")
        );
        String InstructorEmail = existingChapter.getCourse().
                getFormation().getEmailInstructor();
        if (!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to delete this chapter");
        }
        chapterRepository.deleteById(id);
        fileStorageService.delete(FileCategory.VIDEO, existingChapter.getVideoFileName());
        fileStorageService.delete(FileCategory.DOCUMENT, existingChapter.getDocumentFileName());
    }

    @Override
    public Integer getInstructorTotalVideosChapters(Principal principal) {
        String email = principal.getName();
        int result = 0;
        List<Chapter> chapters = chapterRepository.findAll();
        for (Chapter chapter : chapters) {
            if (email.equals(chapter.getCourse().getFormation().getEmailInstructor())) {
                if ( chapter.getVideoFileName()!=null){
                    result ++;
                }
            }
        }
        return result;
    }

    @Override
    public Integer getInstructorTotalResourcesChapters(Principal principal) {
        String email = principal.getName();
        int result = 0;
        List<Chapter> chapters = chapterRepository.findAll();
        for (Chapter chapter : chapters) {
            if (email.equals(chapter.getCourse().getFormation().getEmailInstructor())) {
                if ( chapter.getQuiz()!=null){
                    result ++;
                }
                if ( chapter.getDocumentFileName()!=null){
                    result ++;
                }
            }
        }
        List<Article> articles = articleRepository.findAll();
        for (Article article : articles) {
            if (article.getInstructor().getEmail().equals(email)) {
                if (!article.getImageFileName().isEmpty()){
                    result ++;
                }
            }
        }
        return result;
    }

}
