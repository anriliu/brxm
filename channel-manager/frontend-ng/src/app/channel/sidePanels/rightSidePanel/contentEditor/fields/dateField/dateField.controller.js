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

import moment from 'moment-timezone';

class DateValue {
  constructor(dateString) {
    this._init(moment(dateString));
  }

  _init(initMoment) {
    this.moment = initMoment; // moment for correct timezone handling
    this.jsDate = this.moment.toDate(); // ngModel for md-date-picker
  }

  get hours() {
    return this.moment.hours();
  }

  set hours(hours) {
    this.moment.hours(hours);
  }

  get minutes() {
    return this.moment.minutes();
  }

  set minutes(minutes) {
    this.moment.minutes(minutes);
  }

  get date() {
    return this.jsDate;
  }

  set date(date) {
    const newMoment = moment(date);
    newMoment.hours(this.moment.hours());
    newMoment.minutes(this.moment.minutes());
    this._init(newMoment);
  }

  toDateString() {
    return this.moment.format('YYYY-MM-DDTHH:mm:ss.SSSZ');
  }

  setToNow() {
    this._init(moment());
  }
}

class DateFieldController {
  constructor(FieldService) {
    'ngInject';

    this.FieldService = FieldService;
    this.dateValues = [];
  }

  $onInit() {
    this.onFieldFocus = this.onFieldFocus || angular.noop;
    this.onFieldBlur = this.onFieldBlur || angular.noop;

    for (let i = 0; i < this.fieldValues.length; i += 1) {
      this.dateValues.push(new DateValue(this.fieldValues[i].value));
    }
  }

  getFieldName(index) {
    const fieldName = this.name ? `${this.name}/${this.fieldType.id}` : this.fieldType.id;
    return index > 0 ? `${fieldName}[${index + 1}]` : fieldName;
  }

  valueChanged() {
    this._updateFieldValues();
    this.FieldService.startDraftTimer(this.getFieldName(), this.fieldValues);
  }

  focusDateField($event = null) {
    this.hasFocus = true;
    this.onFieldFocus();

    this.oldValues = angular.copy(this.fieldValues);
    this.FieldService.unsetFocusedInput();

    if ($event) {
      $event.target = angular.element($event.target);
      this.FieldService.setFocusedInput($event.target, $event.customFocus);
    }
  }

  blurDateField($event = null) {
    if ($event) {
      $event.target = angular.element($event.relatedTarget);

      if (this.FieldService.shouldPreserveFocus($event.target)) {
        this.FieldService.triggerInputFocus();
        return;
      }
    }
    delete this.hasFocus;
    this.onFieldBlur();
    this._draftField();
  }

  setToNow(index) {
    this.dateValues[index].setToNow();
    this.valueChanged();
    this.form.$setDirty();
  }

  _updateFieldValues() {
    for (let i = 0; i < this.fieldValues.length; i += 1) {
      this.fieldValues[i].value = this.dateValues[i].toDateString();
    }
  }

  _draftField() {
    if (!angular.equals(this.oldValues, this.fieldValues)) {
      this.FieldService.draftField(this.getFieldName(), this.fieldValues);
    }
  }
}

export default DateFieldController;
