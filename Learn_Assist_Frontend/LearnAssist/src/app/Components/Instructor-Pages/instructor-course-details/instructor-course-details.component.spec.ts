import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstructorCourseDetailsComponent } from './instructor-course-details.component';

describe('InstructorCourseDetailsComponent', () => {
  let component: InstructorCourseDetailsComponent;
  let fixture: ComponentFixture<InstructorCourseDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstructorCourseDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstructorCourseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
