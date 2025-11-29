import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantFormationDetailsComponent } from './participant-formation-details.component';

describe('ParticipantFormationDetailsComponent', () => {
  let component: ParticipantFormationDetailsComponent;
  let fixture: ComponentFixture<ParticipantFormationDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipantFormationDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipantFormationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
