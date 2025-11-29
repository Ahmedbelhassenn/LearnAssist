package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Article;
import com.example.LearnAssist.Models.Chapter;
import com.example.LearnAssist.Repositories.ArticleRepository;
import com.example.LearnAssist.Repositories.ChapterRepository;
import com.example.LearnAssist.Services.ChapterServices;
import com.example.LearnAssist.Services.InscriptionFormationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public class ChapterServicesImpl implements ChapterServices {
    @Autowired
    private ChapterRepository chapterRepository;
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
    public void addChapter(Chapter chapter, String email) {
        String InstructorEmail = chapter.getCourse().
                getFormation().getEmailInstructor();
        if (!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to add this chapter");
        }
        if (chapterRepository.existsByTitle(chapter.getTitle())) {
            throw new ExceptionError("Chapter with this title already exists");
        }

        chapterRepository.save(chapter);
    }

    @Override
    public void editChapter(Long id, Chapter chapter, String email) {
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
        if(chapter.getDocumentFileName()!=null){
            existingChapter.setDocumentFileName(chapter.getDocumentFileName());
        }
        if(chapter.getVideoFileName()!=null){
            existingChapter.setVideoFileName(chapter.getVideoFileName());
        }


        chapterRepository.save(existingChapter);
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
