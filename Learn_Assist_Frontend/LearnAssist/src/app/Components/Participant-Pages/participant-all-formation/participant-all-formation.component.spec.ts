import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantAllFormationComponent } from './participant-all-formation.component';

describe('ParticipantAllFormationComponent', () => {
  let component: ParticipantAllFormationComponent;
  let fixture: ComponentFixture<ParticipantAllFormationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipantAllFormationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipantAllFormationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
