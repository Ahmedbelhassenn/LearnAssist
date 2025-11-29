import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstructorsDetailsComponent } from './instructors-details.component';

describe('InstructorsDetailsComponent', () => {
  let component: InstructorsDetailsComponent;
  let fixture: ComponentFixture<InstructorsDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstructorsDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstructorsDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
