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

class Step1Service {
  constructor(
    ChannelService,
    ContentService,
  ) {
    'ngInject';

    this.ChannelService = ChannelService;
    this.ContentService = ContentService;
    this._reset();
  }

  stop() {
    this._reset();
  }

  open(templateQuery, rootPath, defaultPath) {
    this._reset();

    this.rootPath = this._initRootPath(rootPath);
    this.defaultPath = defaultPath;
    this.templateQuery = templateQuery;

    return this.getTemplateQuery(templateQuery)
      .then(templateQueryResult => this._onLoadDocumentTypes(templateQueryResult.documentTypes))
      .catch(error => this._onError(error, 'Unknown error loading template query'));
  }

  /**
   * Parse the rootPath input value;
   * - use channelRootPath if rootPath is empty
   * - use as is if rootPath is absolute
   * - concatenate with channelRootPath if rootPath is relative
   * - make sure it does not end with a slash
   *
   * @param rootPath the component's rootPath
   */
  _initRootPath(rootPath) {
    const channel = this.ChannelService.getChannel();
    if (!rootPath) {
      return channel.contentRoot;
    }

    if (rootPath.endsWith('/')) {
      rootPath = rootPath.substring(0, rootPath.length - 1);
    }

    if (!rootPath.startsWith('/')) {
      rootPath = `${channel.contentRoot}/${rootPath}`;
    }
    return rootPath;
  }

  createDraft() {
    const draft = {
      name: this.name,
      slug: this.url,
      templateQuery: this.templateQuery,
      documentTypeId: this.documentType,
      rootPath: this.rootPath,
      defaultPath: this.defaultPath,
    };
    return this.ContentService._send('POST', ['documents'], draft);
  }

  getFolders(path) {
    return this.ContentService._send('GET', ['folders', path], null, true);
  }

  getTemplateQuery(id) {
    return this.ContentService._send('GET', ['templatequery', id], null, true);
  }

  _onLoadDocumentTypes(types) {
    this.documentTypes = types;

    if (types.length === 1) {
      this.documentType = types[0].id;
    } else {
      this.documentType = '';
    }
  }

  _reset() {
    delete this.name;
    delete this.url;
    delete this.locale;
    delete this.rootPath;
    delete this.defaultPath;
    delete this.templateQuery;
  }
}

export default Step1Service;
