import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantSignupComponent } from './participant-signup.component';

describe('ParticipantSignupComponent', () => {
  let component: ParticipantSignupComponent;
  let fixture: ComponentFixture<ParticipantSignupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipantSignupComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipantSignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
