import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { TeacherService } from 'src/app/services/teacher.service';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { expect } from '@jest/globals';
import { SessionService } from 'src/app/services/session.service';
import { SessionApiService } from '../../services/session-api.service';

import { FormComponent } from './form.component';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('FormComponent Suite Integration Tests (admin)', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
  let httpMock: HttpTestingController;
  let snackBar: MatSnackBar;
  let router: Router;

  const mockSessionService = { sessionInformation: { admin: true, id: 1 } };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatSelectModule,
        BrowserAnimationsModule,
      ],
      declarations: [FormComponent],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '123' } } } },
        SessionApiService,
        TeacherService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    snackBar = TestBed.inject(MatSnackBar);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should init form in create mode', () => {
    jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/create');
    fixture.detectChanges();

    const reqTeachers = httpMock.expectOne('api/teacher');
    reqTeachers.flush([]);

    expect(component.onUpdate).toBe(false);
    expect(component.sessionForm?.value.name).toBe('');
  });

  it('should init form in update mode and load session', () => {
    jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/update/123');
    fixture.detectChanges();

    const req = httpMock.expectOne('api/session/123');
    req.flush({
      id: '123',
      name: 'Yoga Mania',
      date: '2025-12-13',
      teacher_id: 42,
      description: 'desc',
    });

    expect(component.onUpdate).toBe(true);
    expect(component.sessionForm?.value.name).toBe('Yoga Mania');
  });

  it('should submit in create mode', async () => {
    jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/create');
    fixture.detectChanges();

    const reqTeachers = httpMock.expectOne('api/teacher');
    reqTeachers.flush([]);

    const spySnackBar = jest.spyOn(snackBar, 'open').mockReturnValue({} as any);
    const spyRouter = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    component.sessionForm?.setValue({
      name: 'Test',
      date: '2025-12-13',
      teacher_id: 1,
      description: 'desc',
    });
    component.submit();

    const req = httpMock.expectOne('api/session');
    req.flush({});

    await fixture.whenStable();
    expect(spySnackBar).toHaveBeenCalledWith('Session created !', 'Close', { duration: 3000 });
    expect(spyRouter).toHaveBeenCalledWith(['sessions']);
  });

  it('should submit in update mode', async () => {
    jest.spyOn(router, 'url', 'get').mockReturnValue('/sessions/update/123');
    fixture.detectChanges();

    const pending = httpMock.match(() => true);

    const reqTeachers = pending.find(r => r.request.url === 'api/teacher');
    reqTeachers?.flush([]);

    const reqDetail = pending.find(r => r.request.url === 'api/session/123');
    reqDetail?.flush({
      id: '123',
      name: 'Yoga Mania',
      date: '2025-12-13',
      teacher_id: 42,
      description: 'desc',
    });

    const spySnackBar = jest.spyOn(snackBar, 'open').mockReturnValue({} as any);
    const spyRouter = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    component.sessionForm?.setValue({
      name: 'Yoga Mania Updated',
      date: '2025-12-13',
      teacher_id: 42,
      description: 'desc',
    });
    component.submit();

    const reqUpdate = httpMock.expectOne('api/session/123');
    reqUpdate.flush({});

    await fixture.whenStable();
    expect(spySnackBar).toHaveBeenCalledWith('Session updated !', 'Close', { duration: 3000 });
    expect(spyRouter).toHaveBeenCalledWith(['sessions']);
  });
});

describe('FormComponent Suite Integration Tests (non-admin)', () => {
  let fixture: ComponentFixture<FormComponent>;
  let router: Router;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatSelectModule,
        BrowserAnimationsModule,
      ],
      declarations: [FormComponent],
      providers: [
        { provide: SessionService, useValue: { sessionInformation: { admin: false, id: 1 } } },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '123' } } } },
        SessionApiService,
        TeacherService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FormComponent);
    router = TestBed.inject(Router);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should redirect non-admin user on init', async () => {
    const spyRouter = jest.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    const reqTeachers = httpMock.expectOne('api/teacher');
    reqTeachers.flush([]);

    expect(spyRouter).toHaveBeenCalledWith(['/sessions']);
  });
});