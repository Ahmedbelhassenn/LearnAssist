import { TestBed } from '@angular/core/testing';

import { InscriptionFormationService } from './inscription-formation.service';

describe('InscriptionFormationService', () => {
  let service: InscriptionFormationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InscriptionFormationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
