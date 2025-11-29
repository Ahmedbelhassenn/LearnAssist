import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstructorFormationDetailsComponent } from './instructor-formation-details.component';

describe('InstructorFormationDetailsComponent', () => {
  let component: InstructorFormationDetailsComponent;
  let fixture: ComponentFixture<InstructorFormationDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstructorFormationDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstructorFormationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
