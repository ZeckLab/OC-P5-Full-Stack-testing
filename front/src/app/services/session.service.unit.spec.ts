import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { SessionService } from './session.service';
import { firstValueFrom } from 'rxjs/internal/firstValueFrom';
import { SessionInformation } from '../interfaces/sessionInformation.interface';

describe('SessionService Suite Unit Tests', () => {
  let service: SessionService;
  const mockUser: SessionInformation = { id: 1, token: 'test-token', type: 'Bearer', username: 'test-user', firstName: 'Test', lastName: 'User', admin: false };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SessionService],
    });
    service = TestBed.inject(SessionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it(' should initialize in a logged-out state', async () => {
    expect(service.isLogged).toBe(false);
    expect(service.sessionInformation).toBeUndefined();

    const value = await firstValueFrom(service.$isLogged());
    expect(value).toBe(false);
  });

  it('should should log in and emit true', async () => {
    const spy = jest.spyOn(service as any, 'next');

    service.logIn(mockUser);

    // State updated
    expect(service.isLogged).toBe(true);
    expect(service.sessionInformation).toEqual(mockUser);

    // Observable emits true
    const value = await firstValueFrom(service.$isLogged());
    expect(value).toBe(true);

    // Internal method called
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('should log out and emit false', async () => {
    service.logIn(mockUser);
    const spy = jest.spyOn(service as any, 'next');

    service.logOut();

    // State reset
    expect(service.isLogged).toBe(false);
    expect(service.sessionInformation).toBeUndefined();

    // Observable emits false
    const value = await firstValueFrom(service.$isLogged());
    expect(value).toBe(false);

    // Internal method called
    expect(spy).toHaveBeenCalledTimes(1);
  });
});
