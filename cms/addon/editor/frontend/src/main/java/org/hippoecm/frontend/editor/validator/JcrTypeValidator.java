/*
 *  Copyright 2009 Hippo.
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
package org.hippoecm.frontend.editor.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.model.ocm.StoreException;
import org.hippoecm.frontend.types.IFieldDescriptor;
import org.hippoecm.frontend.types.ITypeDescriptor;
import org.hippoecm.frontend.validation.ValidationException;
import org.hippoecm.frontend.validation.Violation;

/**
 * Validator for generic jcr node types.
 */
public class JcrTypeValidator implements ITypeValidator {
    private static final long serialVersionUID = 1L;

    private Set<JcrFieldValidator> fieldValidators = new HashSet<JcrFieldValidator>();

    public JcrTypeValidator(ITypeDescriptor type) throws StoreException {
        for (IFieldDescriptor field : type.getFields().values()) {
            fieldValidators.add(new JcrFieldValidator(field));
        }
    }

    public Set<Violation> validate(IModel model) throws ValidationException {
        Set<Violation> violations = new HashSet<Violation>();
        for (JcrFieldValidator fieldValidator : fieldValidators) {
            Set<Violation> fieldViolations = fieldValidator.validate(model);
            violations.addAll(fieldViolations);
        }
        return violations;
    }

}
