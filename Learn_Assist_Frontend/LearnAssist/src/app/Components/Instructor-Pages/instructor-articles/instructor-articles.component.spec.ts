import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstructorArticlesComponent } from './instructor-articles.component';

describe('InstructorArticlesComponent', () => {
  let component: InstructorArticlesComponent;
  let fixture: ComponentFixture<InstructorArticlesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstructorArticlesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstructorArticlesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
