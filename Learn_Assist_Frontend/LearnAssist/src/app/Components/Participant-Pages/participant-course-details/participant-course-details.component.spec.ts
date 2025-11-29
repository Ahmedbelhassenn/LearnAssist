import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantCourseDetailsComponent } from './participant-course-details.component';

describe('ParticipantCourseDetailsComponent', () => {
  let component: ParticipantCourseDetailsComponent;
  let fixture: ComponentFixture<ParticipantCourseDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipantCourseDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipantCourseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
