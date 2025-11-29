import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Article } from '../../models/article.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  private baseUrl = environment.apiUrl + '/articles';

  private http= inject(HttpClient);

  getArticlesByInstructor(id: number): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.baseUrl}/instructor/${id}`);
  }

  createArticle(formData: FormData): Observable<any> {
    return this.http.post(this.baseUrl, formData);
  }

  getInstructorArticles(): Observable<Article[]> {
      return this.http.get<Article[]>(this.baseUrl + '/instructor');
  }
  deleteArticle(id: String): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
  getArticleById(id: string): Observable<Article> {
    return this.http.get<Article>(`${this.baseUrl}/${id}`);
  }
  editArticle(id: string, formData: FormData): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, formData);
  }

  getArticleByInstructorId(id: string): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.baseUrl}/instructor/${id}`);
  }

  getAllArticles(): Observable<Article[]> {
    return this.http.get<Article[]>(this.baseUrl);
  }
}
