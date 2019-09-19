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

import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { AppComponent } from './app.component';
import { BootstrapModule } from './bootstrap/bootstrap.module';
import { ClientAppModule } from './client-app/client-app.module';
import { ErrorHandlingModule } from './error-handling/error-handling.module';
import { MainMenuModule } from './main-menu/main-menu.module';
import { NavConfigService } from './services/nav-config.service';
import { SharedModule } from './shared/shared.module';
import { TopPanelModule } from './top-panel/top-panel.module';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  beforeEach(() => {
    fixture = TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        MainMenuModule,
        TopPanelModule,
        ClientAppModule,
        TopPanelModule,
        HttpClientModule,
        SharedModule,
        TopPanelModule,
        BootstrapModule,
        ErrorHandlingModule,
      ],
      declarations: [AppComponent],
      providers: [NavConfigService],
    }).createComponent(AppComponent);

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  });

  it('should create the app', () => {
    expect(component).toBeDefined();
  });
});
