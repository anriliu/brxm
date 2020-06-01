/*
 * Copyright 2015-2017 Hippo B.V. (http://www.onehippo.com)
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

import angular from 'angular';
import 'angular-mocks';

describe('ViewportToggleCtrl', () => {
  let ngModel;
  let viewportMap;
  let $ctrl;
  let $translate;
  let ChannelService;
  let ViewportService;

  beforeEach(() => {
    angular.mock.module('hippo-cm');

    inject((_$componentController_, _$translate_, _ChannelService_, _ViewportService_) => {
      $translate = _$translate_;
      ChannelService = _ChannelService_;
      ViewportService = _ViewportService_;

      ngModel = jasmine.createSpyObj('NgModelCtrl', ['$setViewValue']);
      viewportMap = {
        desktop: 1167,
        tablet: 678,
        phone: 256,
      };
      spyOn($translate, 'instant');
      spyOn(ChannelService, 'getChannel').and.returnValue({ viewportMap });
      spyOn(ViewportService, 'setWidth');

      $ctrl = _$componentController_('viewportToggle', {}, { ngModel });
    });
  });

  describe('$onInit', () => {
    it('selects the "ANY_DEVICE" viewport and uses it to set the viewport', () => {
      $ctrl.$onInit();

      expect($ctrl.value).toBe('ANY_DEVICE');
      expect(ViewportService.setWidth).toHaveBeenCalledWith(0);
    });

    it('should set the viewport widths from the backend', () => {
      $ctrl.$onInit();

      expect($ctrl.values[0].id).toBe('ANY_DEVICE');
      expect($ctrl.values[0].width).toBe(0);
      expect($ctrl.values[1].id).toBe('DESKTOP');
      expect($ctrl.values[1].width).toBe(1167);
      expect($ctrl.values[2].id).toBe('TABLET');
      expect($ctrl.values[2].width).toBe(678);
      expect($ctrl.values[3].id).toBe('PHONE');
      expect($ctrl.values[3].width).toBe(256);
    });

    it('should use the default viewport width values when the backend does not return any', () => {
      Object.keys(viewportMap).forEach(key => delete viewportMap[key]);
      $ctrl.$onInit();

      expect($ctrl.values[0].id).toBe('ANY_DEVICE');
      expect($ctrl.values[0].width).toBe(0);
      expect($ctrl.values[1].id).toBe('DESKTOP');
      expect($ctrl.values[1].width).toBe(1280);
      expect($ctrl.values[2].id).toBe('TABLET');
      expect($ctrl.values[2].width).toBe(720);
      expect($ctrl.values[3].id).toBe('PHONE');
      expect($ctrl.values[3].width).toBe(320);
    });
  });

  describe('onChange', () => {
    beforeEach(() => {
      $ctrl.$onInit();
      $ctrl.value = 'TABLET';
      $ctrl.onChange();
    });

    it('should update the model', () => {
      expect(ngModel.$setViewValue).toHaveBeenCalledWith('TABLET');
    });

    it('should update the viewport width', () => {
      expect(ViewportService.setWidth).toHaveBeenCalledWith(viewportMap.tablet);
    });
  });

  describe('getDisplayName', () => {
    it('should return the display name', () => {
      $ctrl.getDisplayName({ id: 'TEST' });

      expect($translate.instant).toHaveBeenCalledWith('VIEWPORT_TEST');
    });
  });
});
