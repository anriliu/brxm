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

import { Site } from '@bloomreach/navapp-communication';

import { MenuItemContainer } from './main-menu/models/menu-item-container.model';
import { MenuItemLink } from './main-menu/models/menu-item-link.model';
import { MenuItem } from './main-menu/models/menu-item.model';
import { NavItem } from './models/dto/nav-item.dto';
import { GlobalSettings } from './models/global-settings.model';

const item1 = new MenuItemLink('item1', 'Item 1');

const item2 = new MenuItemContainer('Item 2', [
  new MenuItemContainer('Sub item 1', [
    new MenuItemLink('subsubitem1', 'Sub sub item 1'),
    new MenuItemLink('subsubitem2', 'Sub sub item 2'),
    new MenuItemLink('subsubitem3', 'Sub sub item 3'),
  ]),
  new MenuItemLink('subitem2', 'Sub item 2'),
]);

export const menuStructureMock: MenuItem[] = [item1, item2];

export const navConfig: NavItem[] = [
  {
    id: 'subsubitem1',
    appIframeUrl: 'iframe1-url',
    appPath: 'app-path-for-sub-sub-item1',
  },
  {
    id: 'subitem2',
    appIframeUrl: 'iframe2-url',
    appPath: 'app-path-for-sub-item2',
  },
];

export const globalSettings: GlobalSettings = {

  userSettings: {
    userName: 'Frank Zappa',
    language: 'en',
    timeZone: 'Europe/Amsterdam',
  },
  appSettings: {
    navAppLocation: 'http://localhost:4200',
    contextPath: '/cms',
    navConfigResources: [
      {
        resourceType: 'IFRAME',
        url: 'testIFRAMEurl',
      },
      {
        resourceType: 'REST',
        url: 'testRESTurl',
      },
    ],
    loginResources: [],
    logoutResources: [],
  },
};

export const mockSites: Site[] = [
  {
    siteId: -1,
    accountId: 1,
    name: 'www.company.com',
    subGroups: [
      {
        siteId: 1,
        accountId: 1,
        name: 'UK & Germany',
        subGroups: [
          {
            siteId: 2,
            accountId: 1,
            name: 'Office UK',
          },
        ],
      },
    ],
  },
  {
    siteId: -1,
    accountId: 2,
    name:
      'An example company that has a very long name and a subgroup with many items',
    subGroups: [
      {
        siteId: 3,
        accountId: 2,
        name: 'Sub company 001',
      },
      {
        siteId: 4,
        accountId: 2,
        name: 'Sub company 002',
      },
      {
        siteId: 5,
        accountId: 2,
        name: 'Sub company 003',
      },
    ],
  },
];
