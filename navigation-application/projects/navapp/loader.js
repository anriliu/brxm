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

(function NavappLoader() {
  const navAppSettings = window.NavAppSettings;
  let navAppLocation = '.';

  if (
    navAppSettings &&
    navAppSettings.appSettings &&
    navAppSettings.appSettings.navAppLocation
  ) {
    navAppLocation = navAppSettings.appSettings.navAppLocation;
  }

  fetch(`${navAppLocation}/filelist.json`)
    .then(response => response.json())
    .then(files => {
      const urls = Object.values(files);

      urls
        .filter(url => url.endsWith('.css'))
        .forEach(url => {
          const linkTag = document.createElement('link');
          linkTag.rel = 'stylesheet';
          linkTag.href = `${navAppLocation}/${url}`;
          document.head.appendChild(linkTag);
        });

      urls
        .filter(url => url.endsWith('.js'))
        .forEach(url => {
          const scriptTag = document.createElement('script');
          scriptTag.src = `${navAppLocation}/${url}`;
          document.body.appendChild(scriptTag);
        });
    });
})();
