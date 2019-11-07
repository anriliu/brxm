/*
 * Copyright 2019 BloomReach. All rights reserved. (https://www.bloomreach.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { TestBed } from '@angular/core/testing';
import { ClientErrorCodes } from '@bloomreach/navapp-communication';
import { NGXLogger } from 'ngx-logger';
import { LoggerTestingModule } from 'ngx-logger/testing';
import { of } from 'rxjs';

import { ConnectionService } from '../../services/connection.service';
import { AppError } from '../models/app-error';
import { CriticalError } from '../models/critical-error';
import { InternalError } from '../models/internal-error';
import { NotFoundError } from '../models/not-found-error';

import { ErrorHandlingService } from './error-handling.service';

const expectedLoggerMessages = (error: AppError) => [
  `Code: "${error.code}"`,
  `Message: "${error.message}"`,
  `Public description: "${error.description}"`,
  `Description: "${error.internalDescription}"`,
];

describe('ErrorHandlingService', () => {
  let service: ErrorHandlingService;
  let logger: NGXLogger;

  beforeEach(() => {
    const connectionServiceMock = {
      onError$: of(),
    };

    TestBed.configureTestingModule({
      imports: [
        LoggerTestingModule,
      ],
      providers: [
        ErrorHandlingService,
        { provide: ConnectionService, useValue: connectionServiceMock },
      ],
    });

    service = TestBed.get(ErrorHandlingService);
    logger = TestBed.get(NGXLogger);

    spyOn(logger, 'error');
  });

  describe('AppError', () => {
    const expectedError = new AppError(
      500,
      'Some title',
      'Some detailed message',
    );

    beforeEach(() => {
      service.setError(expectedError);
    });

    it('should be set', () => {
      expect(service.currentError).toBe(expectedError);
    });

    it('should forward the error to logger', () => {
      expect(logger.error).toHaveBeenCalledWith(...expectedLoggerMessages(expectedError));
    });
  });

  describe('AppError basing on a client error', () => {
    const expectedError = new AppError(
      500,
      'Something went wrong',
      'Optional message',
    );

    beforeEach(() => {
      service.setClientError(ClientErrorCodes.UnknownError, 'Optional message');
    });

    it('should be set', () => {
      expect(service.currentError).toEqual(expectedError);
    });

    it('should forward the error to logger', () => {
      expect(logger.error).toHaveBeenCalledWith(...expectedLoggerMessages(expectedError));
    });
  });

  describe('CriticalError', () => {
    const expectedError = new CriticalError('Some critical error', 'Description for logs');

    beforeEach(() => {
      service.setCriticalError('Some critical error', 'Description for logs');
    });

    it('should be set', () => {
      expect(service.currentError).toEqual(expectedError);
    });

    it('should forward the error to logger', () => {
      expect(logger.error).toHaveBeenCalledWith(...expectedLoggerMessages(expectedError));
    });
  });

  describe('NotFoundError', () => {
    const expectedError = new NotFoundError('Some available to the user description', 'Description for logs');

    beforeEach(() => {
      service.setNotFoundError('Some available to the user description', 'Description for logs');
    });

    it('should be set', () => {
      expect(service.currentError).toEqual(expectedError);
    });

    it('should forward the error to logger', () => {
      expect(logger.error).toHaveBeenCalledWith(...expectedLoggerMessages(expectedError));
    });
  });

  describe('InternalError', () => {
    const expectedError = new InternalError('Some available to the user description', 'Description for logs');

    beforeEach(() => {
      service.setInternalError('Some available to the user description', 'Description for logs');
    });

    it('should be set', () => {
      expect(service.currentError).toEqual(expectedError);
    });

    it('should forward the error to logger', () => {
      expect(logger.error).toHaveBeenCalledWith(...expectedLoggerMessages(expectedError));
    });
  });

  describe('when the error is set', () => {
    beforeEach(() => {
      const expected = new AppError(
        500,
        'Some title',
        'Some detailed message',
      );

      service.setError(expected);
    });

    it('should clean the error', () => {
      service.clearError();

      expect(service.currentError).toBeUndefined();
    });
  });
});
