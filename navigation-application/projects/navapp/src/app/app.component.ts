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

import { Component, HostBinding, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { ClientAppService } from './client-app/services/client-app.service';
import { NavConfigService } from './services/nav-config.service';
import { OverlayService } from './services/overlay.service';

@Component({
  selector: 'brna-root',
  templateUrl: './app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent implements OnInit {
  @HostBinding('class.mat-typography')
  typography = true;

  constructor(
    private navConfigService: NavConfigService,
    private clientAppService: ClientAppService,
    private overlayService: OverlayService,
  ) {}

  get isOverlayVisible$(): Observable<boolean> {
    return this.overlayService.visible$;
  }

  ngOnInit(): void {
    this.navConfigService.init();
    this.clientAppService.init();
  }
}
