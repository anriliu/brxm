/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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

import { TestBed, ComponentFixture, async, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/toPromise';

import { CreateContentService } from '../create-content.service';
import { HintsComponent } from '../../../../../shared/components/hints/hints.component';
import { NameUrlFieldsComponent } from '../name-url-fields/name-url-fields.component';
import { SharedModule } from '../../../../../shared/shared.module';
import {
  ContentServiceMock, CreateContentServiceMock, DialogServiceMock, FieldServiceMock,
  MdDialogMock
} from '../create-content.mocks.spec';
import { CreateContentStep2Component } from './step-2.component';
import { SharedspaceToolbarDirective } from '../../fields/ckeditor/sharedspace-toolbar/sharedspace-toolbar.component';
import { FieldsEditorDirective } from '../../fieldsEditor/fields-editor.component';
import { TranslateModule, TranslateLoader, TranslateFakeLoader } from '@ngx-translate/core';

import ContentService from '../../../../../services/content.service';
import DialogService from '../../../../../services/dialog.service';
import FieldService from '../../fields/field.service';
import { DocumentTypeInfo, Document } from '../create-content.types';
import { MdDialogRef } from "@angular/material";
import { NameUrlFieldsDialogComponent } from './name-url-fields-dialog/name-url-fields-dialog';
import { BrowserDynamicTestingModule } from "@angular/platform-browser-dynamic/testing";

describe('Create content step 2 component', () => {
  let component: CreateContentStep2Component;
  let fixture: ComponentFixture<CreateContentStep2Component>;
  let createContentService: CreateContentService;
  let contentService: ContentService;
  let dialogService: DialogService;
  let fieldService: FieldService;
  let dialog: MdDialogRef;

  const testDocument: Document = {
    id: 'testId',
    displayName: 'test document',
    info: {
      dirty: false,
      type: {
        id: 'ns:testdocument',
      }
    }
  };
  const testDocumentType: DocumentTypeInfo = { id: 'ns:testdocument', displayName: 'test-name 1' };
  const resolve = (arg = null) => Observable.of(arg).toPromise();
  const reject = (arg = null) => Observable.throw(arg).toPromise();

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        CreateContentStep2Component,
        HintsComponent,
        NameUrlFieldsComponent,
        SharedspaceToolbarDirective,
        FieldsEditorDirective,
        NameUrlFieldsDialogComponent
      ],
      imports: [
        SharedModule,
        FormsModule,
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
        })
      ],
      providers: [
        { provide: CreateContentService, useClass: CreateContentServiceMock },
        { provide: ContentService, useClass: ContentServiceMock },
        { provide: DialogService, useClass: DialogServiceMock },
        { provide: FieldService, useClass: FieldServiceMock },
        { provide: MdDialogRef, useClass: MdDialogMock }
      ]
    });

    TestBed.overrideModule(BrowserDynamicTestingModule, {
      set: {
        entryComponents: [NameUrlFieldsDialogComponent],
      },
    });

    fixture = TestBed.createComponent(CreateContentStep2Component);
    component = fixture.componentInstance;
    createContentService = fixture.debugElement.injector.get(CreateContentService);
    contentService = fixture.debugElement.injector.get(ContentService);
    dialogService = fixture.debugElement.injector.get(DialogService);
    fieldService = fixture.debugElement.injector.get(FieldService);
    dialog = fixture.debugElement.injector.get(MdDialogRef);

    spyOn(contentService, 'getDocumentType').and.callThrough();
    spyOn(createContentService, 'getDocument').and.callThrough();
    spyOn(dialogService, 'confirm').and.callThrough();

    fixture.detectChanges();
  });

  it('should detect ESC keypress', fakeAsync(() => {
    fixture.detectChanges();
    spyOn(component, 'close');
    const event = new KeyboardEvent('keypress');
    Object.defineProperty(event, 'which', { value: 27 });
    fixture.nativeElement.dispatchEvent(event);

    expect(component.close).toHaveBeenCalled();
  }));

  it('should call parent "on full width" mode on and off', () => {
    spyOn(component.onFullWidth, 'emit');
    component.setFullWidth(true);
    expect(component.isFullWidth).toBe(true);
    expect(component.onFullWidth.emit).toHaveBeenCalledWith(true);

    component.setFullWidth(false);
    expect(component.isFullWidth).toBe(false);
    expect(component.onFullWidth.emit).toHaveBeenCalledWith(false);
  });

  it('on init, loads the document from the createContentService', () => {
    spyOn(component,'loadNewDocument');
    spyOn(component,'resetBeforeStateChange');

    component.ngOnInit();
    expect(component.loadNewDocument).toHaveBeenCalled();
    expect(component.resetBeforeStateChange).toHaveBeenCalled();
  });

  describe('opening a document', () => {
    beforeEach(() => {
      createContentService.getDocument.and.callThrough();
      contentService.getDocumentType.and.callThrough();
    });

    it('gets the newly created draft document from create content service', () => {
      component.loadNewDocument();
      expect(createContentService.getDocument).toHaveBeenCalled();
      expect(contentService.getDocumentType).toHaveBeenCalledWith('ns:testdocument');
    });

    it('gets the newly created draft document from create content service', fakeAsync(() => {
      spyOn(component, 'onLoadSuccess');
      component.loadNewDocument();

      tick();
      fixture.detectChanges();

      expect(component.onLoadSuccess).toHaveBeenCalledWith(testDocument, testDocumentType);
      expect(component.loading).toEqual(false);
    }));
  });

  describe('closing the panel', () => {
    beforeEach(() => {
      spyOn(component, 'resetState');
      spyOn(component.onClose, 'emit');

      component.ngOnInit();
    });

    it('Calls discardAndClose method to confirm document discard and close the panel', fakeAsync(() => {
      tick();
      fixture.detectChanges();

      spyOn(component, 'discardAndClose').and.returnValue(resolve());

      component.close();
      expect(component.discardAndClose).toHaveBeenCalled();
    }));

    it('Discards the document when "discard" is selected', fakeAsync(() => {
      tick();
      fixture.detectChanges();
      spyOn(component, 'discardAndClose').and.returnValue(resolve());
      spyOn(component, 'confirmDiscardChanges').and.returnValue(resolve());

      component.close().then(() => {
        expect(component.resetState).toHaveBeenCalled();
        expect(component.onClose.emit).toHaveBeenCalled();
      });
    }));

    it('Will not discard the document when cancel is clicked', fakeAsync(() => {
      tick();
      fixture.detectChanges();

      spyOn(component, 'confirmDiscardChanges').and.returnValue(reject());

      component.close().catch(() => {
        expect(component.resetState).not.toHaveBeenCalled();
        expect(component.onClose.emit).not.toHaveBeenCalled();
      });
    }));
  });

  describe('changing name or URL of the document', () => {
    beforeEach(() => {
      spyOn(component, 'openEditNameUrlDialog').and.callThrough();
      spyOn(component.dialog, 'open').and.returnValue(dialog);
      spyOn(dialog, 'afterClosed').and.returnValue(Observable.of({ name: 'docName', url: 'doc-url' }));
      spyOn(component, 'submitEditNameUrl').and.callThrough();

      component.ngOnInit();
      component.doc = testDocument;
    });

    it('open a change url-name dialog', fakeAsync(() => {
      tick();
      fixture.detectChanges();
      component.editNameUrl();

      expect(component.openEditNameUrlDialog).toHaveBeenCalled();
    }));

    it('openEditNameUrlDialog method open a dialog with the correct details', () => {
      component.openEditNameUrlDialog();

      const dialogProps = {
        height: '250px',
        width: '600px',
        data: {
          title: 'CHANGE_DOCUMENT_NAME',
          name: testDocument.displayName,
          url: '',
        }
      };

      expect(component.dialog.open).toHaveBeenCalledWith(NameUrlFieldsDialogComponent, dialogProps);
    });

    it('changes document title if the change is submitted in dialog', fakeAsync(() => {
      component.editNameUrl();
      tick();
      fixture.detectChanges();

      expect(component.submitEditNameUrl).toHaveBeenCalledWith({ name: 'docName', url: 'doc-url' });
    }));

    it('takes no action if user clicks cancel on the dialog', fakeAsync(() => {
      dialog.afterClosed.and.returnValue(Observable.of(false));
      tick();
      fixture.detectChanges();

      component.editNameUrl();

      expect(component.submitEditNameUrl).not.toHaveBeenCalled();
    }));
  });


  it('knows the document is dirty when the backend says so', () => {
    component.doc = testDocument;
    component.doc.info.dirty = true;
    expect(component.isDocumentDirty()).toBe(true);
  });
});
