/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
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

class SpaService {
  constructor($log, OverlayService, ChannelRenderingService) {
    'ngInject';

    this.$log = $log;
    this.OverlayService = OverlayService;
    this.ChannelRenderingService = ChannelRenderingService;
  }

  init(iframeJQueryElement) {
    this.iframeJQueryElement = iframeJQueryElement;
  }

  detectSpa() {
    this.spa = this.iframeJQueryElement.contentWindow.SPA;
    return angular.isDefined(this.spa);
  }

  initSpa() {
    try {
      const publicApi = {
        createOverlay: () => {
          this.ChannelRenderingService.createOverlay();
        },
        syncOverlay: () => {
          this.OverlayService.sync();
        },
      };
      this.spa.init(publicApi);
    } catch (error) {
      this.$log.error('Failed to initialize Single Page Application', error);
    }
  }

  renderComponent(componentId, parameters = {}) {
    if (this.spa && angular.isFunction(this.spa.renderComponent)) {
      // let the SPA render the component; if it returns false, we still render the component instead
      return this.spa.renderComponent(componentId, parameters) !== false;
    }
    return false;
  }
}

export default SpaService;
