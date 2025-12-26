import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Teacher } from '../interfaces/teacher.interface';
import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { TeacherService } from './teacher.service';

describe('TeacherService Suite Unit Tests', () => {
  let service: TeacherService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TeacherService]
    });

    service = TestBed.inject(TeacherService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all teachers', () => {
    const spy = jest.spyOn(service, 'all');
    const mockTeachers: Teacher[] = [
      { id: 1, firstName: 'Maya', lastName: 'Labeille', createdAt: new Date(), updatedAt: new Date() },
      { id: 2, firstName: 'Bob', lastName: 'Léponge', createdAt: new Date(), updatedAt: new Date() }
    ];

    service.all().subscribe((teachers) => {
      expect(teachers).toEqual(mockTeachers);
      expect(spy).toHaveBeenCalledTimes(1);
    });

    const req = httpMock.expectOne('api/teacher');
    expect(req.request.method).toBe('GET');
    req.flush(mockTeachers);
  });

  it('should fetch a teacher by id', () => {
    const spy = jest.spyOn(service, 'detail');
    const mockTeacher: Teacher = {
      id: 1, firstName: 'Maya', lastName: 'Labeille', createdAt: new Date(), updatedAt: new Date()
    };

    service.detail('1').subscribe((teacher) => {
      expect(teacher).toEqual(mockTeacher);
      expect(spy).toHaveBeenCalledWith('1');
    });

    const req = httpMock.expectOne('api/teacher/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockTeacher);
  });

  it('should return 404 when teacher is not found', () => {
    // GIVEN
    const errorMessage = 'Teacher not found';

    // WHEN
    service.detail('666').subscribe({
      next: () => fail('Expected an error, not a teacher'),
      error: (error) => {
        // THEN
        expect(error.status).toBe(404);
        expect(error.statusText).toBe('Not Found');
      }
    });

    // WHEN he request is intercepted by HttpTestingController
    const req = httpMock.expectOne('api/teacher/666');
    expect(req.request.method).toBe('GET');

    // THEN we simulate the 404 response
    req.flush(errorMessage, { status: 404, statusText: 'Not Found' });
  });

});

