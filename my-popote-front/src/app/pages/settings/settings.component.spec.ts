import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { AuthResponse } from '../../models/auth.model';
import { SettingsComponent } from './settings.component';

describe('SettingsComponent', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;

  let routerSpy: jasmine.SpyObj<Router>;

  const currentUser: AuthResponse = {
    email: 'toto.test@gmail.com',
    firstName: 'Toto',
    accessToken: 'test-token',
  };

  const authServiceMock = {
    getCurrentUser: jasmine
      .createSpy('getCurrentUser')
      .and.returnValue(currentUser),
    logout: jasmine.createSpy('logout'),
  };

  beforeEach(async () => {
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);

    authServiceMock.getCurrentUser.calls.reset();
    authServiceMock.getCurrentUser.and.returnValue(currentUser);
    authServiceMock.logout.calls.reset();

    await TestBed.configureTestingModule({
      imports: [SettingsComponent],
      providers: [
        {
          provide: AuthService,
          useValue: authServiceMock,
        },
        {
          provide: Router,
          useValue: routerSpy,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose the authenticated user', () => {
    expect(component.user).toEqual(currentUser);
  });

  it('should clear the session and redirect to sign in on logout', () => {
    component.logout();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/sign-in']);
  });
});
