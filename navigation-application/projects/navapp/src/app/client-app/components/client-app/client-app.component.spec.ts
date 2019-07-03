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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CommunicationsService } from '../../../services/communications.service';
import { ClientApp } from '../../models/client-app.model';
import { ClientAppService } from '../../services/client-app.service';

import { ClientAppComponent } from './client-app.component';

describe('ClientAppComponent', () => {
  let component: ClientAppComponent;
  let fixture: ComponentFixture<ClientAppComponent>;

  const clientAppService = jasmine.createSpyObj(['addConnection']);
  const communicationsService = jasmine.createSpyObj('CommunicationsService', [
    'parentApiMethods',
  ]);

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: ClientAppService, useValue: clientAppService },
        { provide: CommunicationsService, useValue: communicationsService },
      ],
      declarations: [ClientAppComponent],
    });
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ClientAppComponent);
    component = fixture.componentInstance;
    component.app = new ClientApp('mytesturl');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
