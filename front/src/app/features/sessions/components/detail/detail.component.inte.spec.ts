import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionApiService } from '../../services/session-api.service';
import { TeacherService } from '../../../../services/teacher.service';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { expect } from '@jest/globals';
import { SessionService } from '../../../../services/session.service';

import { DetailComponent } from './detail.component';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

describe('DetailComponent Suite Integration Tests', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;
  let httpMock: HttpTestingController;
  let snackBar: MatSnackBar;
  let router: Router;

  const mockSessionService = {
    sessionInformation: { admin: true, id: 1 },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
      ],
      declarations: [DetailComponent],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '123' } } },
        },
        SessionApiService,
        TeacherService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    snackBar = TestBed.inject(MatSnackBar);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch session and teacher on init, and render delete button for admin', () => {
    const mockSession = { id: '123', teacher_id: 42, users: [1] } as any;
    const mockTeacher = { id: 42, firstName: 'Maya', lastName: 'Labeille' } as any;

    fixture.detectChanges(); // triggers ngOnInit -> fetchSession()

    const reqSession = httpMock.expectOne('api/session/123');
    expect(reqSession.request.method).toBe('GET');
    reqSession.flush(mockSession);

    const reqTeacher = httpMock.expectOne('api/teacher/42');
    expect(reqTeacher.request.method).toBe('GET');
    reqTeacher.flush(mockTeacher);

    // Functional test: check component state
    expect(component.session).toEqual(mockSession);
    expect(component.teacher).toEqual(mockTeacher);
    expect(component.isParticipate).toBe(true);
    expect(component.isAdmin).toBe(true);

    fixture.detectChanges();

    // Integration test: check rendered template
    const deleteBtn = fixture.nativeElement.querySelector('button[color="warn"] span');
    expect(deleteBtn.textContent).toContain('Delete');
  });

  it('should delete session, show snackbar and navigate', () => {
    const spySnackBar = jest.spyOn(snackBar, 'open').mockReturnValue({} as any);
    const spyRouter = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    component.sessionId = '123';
    component.delete();

    const req = httpMock.expectOne('api/session/123');
    expect(req.request.method).toBe('DELETE');
    req.flush({});

    expect(spySnackBar).toHaveBeenCalledWith('Session deleted !', 'Close', {
      duration: 3000,
    });
    expect(spyRouter).toHaveBeenCalledWith(['sessions']);
  });

  it('should participate and refresh session', () => {
    component.session = { id: '123', teacher_id: 42, users: [] } as any;
    component.userId = '1';
    component.isAdmin = false;
    component.isParticipate = false;

    fixture.detectChanges();

    // Flush initial GET triggered by ngOnInit
    const initSession = httpMock.expectOne('api/session/123');
    initSession.flush({ id: '123', teacher_id: 42, users: [] });
    const initTeacher = httpMock.expectOne('api/teacher/42');
    initTeacher.flush({ id: 42, username: 'teacher1' });

    // Verify the initial button state
    let participateBtn: HTMLButtonElement = fixture.nativeElement.querySelector('button[color="primary"]');
    expect(participateBtn.textContent).toContain('Participate');

    // Simulate the click
    participateBtn.click();

    // POST
    const postReq = httpMock.expectOne('api/session/123/participate/1');
    expect(postReq.request.method).toBe('POST');
    postReq.flush({});

    // refresh GET
    const refreshSession = httpMock.expectOne('api/session/123');
    refreshSession.flush({ id: '123', teacher_id: 42, users: [1] });
    const refreshTeacher = httpMock.expectOne('api/teacher/42');
    refreshTeacher.flush({ id: 42, username: 'teacher1' });

    fixture.detectChanges();

    // Verify that the button has changed to ‘Do not participate’
    const unParticipateBtn: HTMLButtonElement = fixture.nativeElement.querySelector('button[color="warn"]');
    expect(unParticipateBtn.textContent).toContain('Do not participate');
  });
});
