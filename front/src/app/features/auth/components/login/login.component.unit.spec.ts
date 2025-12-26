import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { expect } from '@jest/globals';
import { SessionService } from 'src/app/services/session.service';
import { LoginComponent } from './login.component';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let httpMock: HttpTestingController;
  let router: Router;
  let sessionService: SessionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [SessionService],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        ReactiveFormsModule,
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    sessionService = TestBed.inject(SessionService);
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should login successfully', async () => {
    const spyRouter = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    component.form.setValue({ email: 'admin@test.com', password: 'secret' });
    component.submit();

    const req = httpMock.expectOne('api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, name: 'Admin' });

    await fixture.whenStable();
    expect(spyRouter).toHaveBeenCalledWith(['/sessions']);

    expect(sessionService.isLogged).toBe(true);
    expect(sessionService.sessionInformation).toEqual({ id: 1, name: 'Admin' });

  });

  it('should display error message on bad credentials', () => {
    component.form.setValue({ email: 'wrong@test.com', password: 'bad' });
    component.submit();

    const req = httpMock.expectOne('api/auth/login');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(component.onError).toBe(true);

    fixture.detectChanges();

    const errorMsg = fixture.nativeElement.querySelector('p.error');
    expect(errorMsg).toBeTruthy();
    expect(errorMsg.textContent).toContain('An error occurred');
  });
});
