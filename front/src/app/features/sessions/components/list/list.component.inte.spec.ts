import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { expect } from '@jest/globals';
import { SessionService } from 'src/app/services/session.service';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';

import { ListComponent } from './list.component';
import { SessionApiService } from '../../services/session-api.service';

describe('ListComponent Suite Integration Tests', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;
  let sessionService: SessionService;

  const mockSessions = [
    { id: 1, name: 'Yoga', description: 'Morning yoga', date: new Date(), teacher_id: 1, users: [] },
    { id: 2, name: 'Pilates', description: 'Evening pilates', date: new Date(), teacher_id: 2, users: [] },
  ];

  const mockSessionApiService = {
    all: jest.fn().mockReturnValue(of(mockSessions))
  };


  beforeEach(async () => {
    const apiMock: jest.Mocked<SessionApiService> = {
      all: jest.fn().mockReturnValue(of(mockSessions))
    } as any;

    await TestBed.configureTestingModule({
      declarations: [ListComponent],
      imports: [MatCardModule, MatIconModule, RouterTestingModule],
      providers: [
        SessionService,
        { provide: SessionApiService, useValue: mockSessionApiService }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
    sessionService = TestBed.inject(SessionService);
    sessionService.sessionInformation = { admin: true } as any;
    fixture.detectChanges();
  });

  it('should display sessions list', () => {
    fixture.detectChanges();
    const titles = fixture.debugElement.queryAll(By.css('.item mat-card-title'));
    expect(titles.length).toBe(2);
    expect(titles[0].nativeElement.textContent).toContain('Yoga');
    expect(titles[1].nativeElement.textContent).toContain('Pilates');
  });

  it('should show Create and Edit buttons if user is admin', () => {
    sessionService.sessionInformation = { admin: true } as any;
    fixture.detectChanges();

    const createBtn = fixture.debugElement.query(By.css('button[routerLink="create"]'));
    expect(createBtn).toBeTruthy();

    const editButtons = fixture.debugElement
      .queryAll(By.css('button'))
      .filter(el => el.nativeElement.textContent.includes('Edit'));

    expect(editButtons.length).toEqual(mockSessions.length);
  });

  it('should not show Create and Edit buttons if user is not admin', () => {
    sessionService.sessionInformation = { admin: false } as any;
    fixture.detectChanges();

    const createBtn = fixture.debugElement.query(By.css('button[routerLink="create"]'));
    expect(createBtn).toBeNull();

    const editBtns = fixture.debugElement
      .queryAll(By.css('button'))
      .filter(el => el.nativeElement.textContent.includes('Edit'));
    expect(editBtns.length).toBe(0);
  });

  it('should always show Detail buttons', () => {
    fixture.detectChanges();
    const detailBtns = fixture.debugElement
      .queryAll(By.css('button'))
      .filter(el => el.nativeElement.textContent.includes('Detail'));
    expect(detailBtns.length).toBe(mockSessions.length);
  });
});
