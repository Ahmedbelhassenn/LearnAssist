import { Component } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { SafeUrlPipe } from '../../../safe-url.pipe';
import { ActivatedRoute } from '@angular/router';
import { ChapterService } from '../../../services/chapter/chapter.service';
import { CourseService } from '../../../services/course/course.service';
import { FilesService } from '../../../services/files/files.service';
import { ServiceService } from '../../../services/service.service';
@Component({
  selector: 'app-participant-course-details',
  standalone: true,
  imports: [NgFor, NgIf, SafeUrlPipe],
  templateUrl: './participant-course-details.component.html',
  styleUrl: './participant-course-details.component.css'
})
export class ParticipantCourseDetailsComponent {
  selectedCourse: any;
  expandedChapterId: number | null = null;

  

  constructor(private courseService: CourseService, private route: ActivatedRoute, private fileService: FilesService,
      private service:ServiceService, private chapterService: ChapterService) {}

  ngOnInit(): void {
    this.getInformation()
  }

  getInformation():void{
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
    this.courseService.getCourseById(id).subscribe(response => {
      this.selectedCourse = response.course;
      this.selectedCourse.title=this.service.capitalizeFirstLetter(this.selectedCourse.title)

      // Construire les URLs des fichiers pour chaque chapitre
      this.selectedCourse.chapters.forEach((chapter: any) => {
        chapter.title=this.service.capitalizeFirstLetter(chapter.title)
        if (chapter.videoFileName) {
          this.fileService.getChapterVideo(chapter.videoFileName).subscribe(
            (blob) => {
              const objectURL = URL.createObjectURL(blob);
              chapter.videoUrl = objectURL;
            },
            (error) => {
              console.error("Erreur lors du chargement du vidéo", error);
            }
          );
        }

        if (chapter.documentFileName) {
          this.fileService.getChapterDocument(chapter.documentFileName).subscribe(
            (blob) => {
              const objectURL = URL.createObjectURL(blob);
              chapter.documentUrl = objectURL;
            },
            (error) => {
              console.error("Erreur lors du chargement du vidéo", error);
            }
          );
        }
      });
    });
  }
  }

  toggleChapter(chapterId: number) {
    this.expandedChapterId = this.expandedChapterId === chapterId ? null : chapterId;
  }
}
