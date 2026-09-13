import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { WeekService } from './week.service';

describe('WeekService', () => {
  let service: WeekService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()],
    });

    service = TestBed.inject(WeekService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
