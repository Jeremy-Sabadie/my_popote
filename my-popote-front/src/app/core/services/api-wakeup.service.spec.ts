import { TestBed } from '@angular/core/testing';

import { ApiWakeupService } from './api-wakeup.service';

describe('ApiWakeupService', () => {
  let service: ApiWakeupService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApiWakeupService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
