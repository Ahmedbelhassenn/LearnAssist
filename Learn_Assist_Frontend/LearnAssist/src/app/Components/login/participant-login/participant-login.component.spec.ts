import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantLoginComponent } from './participant-login.component';

describe('ParticipantLoginComponent', () => {
  let component: ParticipantLoginComponent;
  let fixture: ComponentFixture<ParticipantLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipantLoginComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipantLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
