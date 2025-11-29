import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantLayoutComponent } from './participant-layout.component';

describe('ParticipantLayoutComponent', () => {
  let component: ParticipantLayoutComponent;
  let fixture: ComponentFixture<ParticipantLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipantLayoutComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipantLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
