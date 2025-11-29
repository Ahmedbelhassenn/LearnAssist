import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantHomePageComponent } from './participant-home-page.component';

describe('ParticipantHomePageComponent', () => {
  let component: ParticipantHomePageComponent;
  let fixture: ComponentFixture<ParticipantHomePageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipantHomePageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipantHomePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
