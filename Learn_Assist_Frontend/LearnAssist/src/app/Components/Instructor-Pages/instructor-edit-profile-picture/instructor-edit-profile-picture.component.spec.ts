import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstructorEditProfilePictureComponent } from './instructor-edit-profile-picture.component';

describe('InstructorEditProfilePictureComponent', () => {
  let component: InstructorEditProfilePictureComponent;
  let fixture: ComponentFixture<InstructorEditProfilePictureComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstructorEditProfilePictureComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstructorEditProfilePictureComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
