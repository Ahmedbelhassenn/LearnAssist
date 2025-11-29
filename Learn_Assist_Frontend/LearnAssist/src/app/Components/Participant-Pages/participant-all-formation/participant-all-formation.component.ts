import { Component } from '@angular/core';
import { ParticipantNavbarComponent } from "../participant-navbar/participant-navbar.component";
import { NgFor } from '@angular/common';

@Component({
  selector: 'app-participant-all-formation',
  standalone: true,
  imports: [ NgFor],
  templateUrl: './participant-all-formation.component.html',
  styleUrl: './participant-all-formation.component.css'
})
export class ParticipantAllFormationComponent {
  categories = ['Tout', 'Développement', 'Design', 'Business', 'Marketing'];
  selectedCategory = 'Tout';
  
  formations = [
    {
      id: 1,
      title: 'Développement Web Moderne',
      description: 'Apprenez HTML, CSS, JavaScript et plus encore...',
      duration: 12,
      imageUrl: 'https://source.unsplash.com/featured/?coding',
      category: 'Développement'
    },
    {
      id: 2,
      title: 'Introduction au Design UI/UX',
      description: 'Créez des expériences utilisateurs incroyables.',
      duration: 8,
      imageUrl: 'https://source.unsplash.com/featured/?design',
      category: 'Design'
    },
    {
      id: 3,
      title: 'Stratégies de Marketing Digital',
      description: 'Boostez votre visibilité en ligne facilement.',
      duration: 6,
      imageUrl: 'https://source.unsplash.com/featured/?marketing',
      category: 'Marketing'
    },
    // Ajoute d'autres formations ici
  ];
  
  selectCategory(category: string) {
    this.selectedCategory = category;
  }
  
  filteredFormations() {
    if (this.selectedCategory === 'Tout') {
      return this.formations;
    }
    return this.formations.filter(f => f.category === this.selectedCategory);
  }
  
  goToFormationDetails(id: number) {
    // Navigation vers la page détail
    console.log('Voir détail de la formation', id);
  }
  
}
