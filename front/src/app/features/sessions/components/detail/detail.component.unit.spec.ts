import { ComponentFixture, TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';
import { RouterTestingModule } from '@angular/router/testing';
import { DetailComponent } from './detail.component';
import { SessionService } from '../../../../services/session.service';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { SessionApiService } from '../../services/session-api.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TeacherService } from 'src/app/services/teacher.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';


describe('DetailComponent Suite Unit Tests', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;
  let sessionApiService: SessionApiService;

  const mockSessionService = {
    sessionInformation: { admin: true, id: 1 },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        ReactiveFormsModule,
        MatSnackBarModule
      ],
      declarations: [DetailComponent],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: SessionApiService, useValue: {
            participate: jest.fn().mockReturnValue(of({} as any)),
            delete: jest.fn().mockReturnValue(of({} as any)),
            unParticipate: jest.fn().mockReturnValue(of({} as any)),
            detail: jest.fn().mockReturnValue(of({
              id: 123,
              name: 'La Reine du Yoga',
              description: 'Une session de yoga pour débutants',
              date: new Date(),
              teacher_id: 1,
              users: []
            })),
        }},
        { provide: TeacherService, useValue: {
          detail: jest.fn().mockReturnValue(of({
            id: 1,
            firstName: 'Maya',
            lastName: 'Labeille',
            createdAt: new Date(),
            updatedAt: new Date()
          })),
        }
      }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    sessionApiService = TestBed.inject(SessionApiService);

    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call window.history.back when back() is invoked', () => {
    window.history.back = jest.fn();

    component.back();

    expect(window.history.back).toHaveBeenCalled();
  });

  it('should initialize admin and userId from SessionService', () => {
    expect(component.isAdmin).toBe(true);
    expect(component.userId).toBe('1');
  });

  it('should call unParticipate() on the API and then fetchSession', () => {
    // GIVEN
    const apiSpy = jest.spyOn(sessionApiService, 'unParticipate').mockReturnValue(of({} as any));
    const fetchSpy = jest.spyOn(component as any, 'fetchSession');
    component.sessionId = '123';
    component.userId = '1';

    // WHEN
    component.unParticipate();

    // THEN
    expect(apiSpy).toHaveBeenCalledWith('123', '1');
    expect(fetchSpy).toHaveBeenCalled();
  });

});
