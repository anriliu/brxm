/*
 *  Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.validation;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.types.IFieldDescriptor;
import org.hippoecm.frontend.types.ITypeDescriptor;

/**
 * Interface implemented by the CMS validation mechanism to enable custom validators to check values and report
 * violations.
 */
public interface IFieldValidator extends IClusterable {

    IFieldDescriptor getFieldDescriptor();

    ITypeDescriptor getFieldType();

    Violation newValueViolation(IModel childModel, String translation) throws ValidationException;

}
