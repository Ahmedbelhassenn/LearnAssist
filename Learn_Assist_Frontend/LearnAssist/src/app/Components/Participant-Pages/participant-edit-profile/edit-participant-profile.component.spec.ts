import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditParticipantProfileComponent } from './edit-participant-profile.component';

describe('EditParticipantProfileComponent', () => {
  let component: EditParticipantProfileComponent;
  let fixture: ComponentFixture<EditParticipantProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditParticipantProfileComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditParticipantProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
