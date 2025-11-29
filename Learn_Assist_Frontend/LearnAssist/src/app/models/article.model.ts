export interface Article {
  id?: string;
  title: string;
  content: string;
  imageFileName?: string;
  imageblob?: string; // pour gérer le nom du fichier image côté backend
  publishedAt?: Date;
  instructorName?: string; // pour affichage côté frontend
}
