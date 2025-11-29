import { Component } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { SafeUrlPipe } from '../../../safe-url.pipe';
import { CourseService } from '../../../services/course/course.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FilesService } from '../../../services/files/files.service';
import { ServiceService } from '../../../services/service.service';
import { ChapterService } from '../../../services/chapter/chapter.service';
import { AddChapterComponent } from '../../formation-Pages/add-chapter/add-chapter.component';
import { EditChapterComponent } from '../../formation-Pages/edit-chapter/edit-chapter.component';

@Component({
  selector: 'app-instructor-course-details',
  standalone: true,
  imports: [NgFor, NgIf, SafeUrlPipe, AddChapterComponent, EditChapterComponent],
  templateUrl: './instructor-course-details.component.html',
  styleUrl: './instructor-course-details.component.css'
})
export class InstructorCourseDetailsComponent {

  idChapter='';
  showDeleteModal= false;
  selectedCourse: any;
  showAddChapterModal=false;
  showEditModal=false;
  expandedChapterId: number | null = null;

  toggleChapter(chapterId: number) {
    this.expandedChapterId = this.expandedChapterId === chapterId ? null : chapterId;
  }
  constructor(private courseService: CourseService, private route: ActivatedRoute, private fileService: FilesService,
    private service:ServiceService, private chapterService: ChapterService
  ) {}

  ngOnInit(): void {
    this.getInformation()
  }

  openAddChapterModal() {
    this.showAddChapterModal=true;
  }

  closeAddChapterModal() {
    this.showAddChapterModal=false;
    this.getInformation();
  }
  openDeleteModal(id: string) {
    this.idChapter = id;
    this.showDeleteModal = true;
  }

  openEditChapterModal(id: string) {
    localStorage.setItem('chapterId', id);
    this.showEditModal = true;
  } 

  closeEditChapterModal(){
    this.showEditModal = false;
    this.getInformation()
  }
deleteChapter() {
  this.chapterService.deleteChapter(this.idChapter).subscribe({
          next: () => {
            console.log("Chapitre supprimé");
            this.showDeleteModal = false; 
            this.getInformation()// rafraîchir la liste
          },
          error: (error) => {
            console.error("Erreur lors de la suppression :", error);
          }
  });
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
}
