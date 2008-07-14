/*
 *  Copyright 2008 Hippo.
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
package org.hippoecm.repository.jackrabbit.xml;

import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.nodetype.EffectiveNodeType;
import org.apache.jackrabbit.core.nodetype.PropDef;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.xml.Importer;
import org.apache.jackrabbit.core.xml.TextValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Information about a property being imported. This class is used
 * by the XML import handlers to pass the parsed property information
 * through the {@link Importer} interface to the actual import process.
 * <p>
 * In addition to carrying the actual property data, instances of this
 * class also know how to apply that data when imported either to a
 * {@link NodeImpl} instance through a session or directly to a
 * {@link NodeState} instance in a workspace.
 */
public class PropInfo {

    @SuppressWarnings("unused")
    final static String SVN_ID = "$Id$";
    
    /**
     * Logger instance.
     */
    private static Logger log = LoggerFactory.getLogger(PropInfo.class);

    /**
     * Name of the property being imported.
     */
    private final Name name;

    /**
     * Type of the property being imported.
     */
    private final int type;

    /**
     * Value(s) of the property being imported.
     */
    private final TextValue[] values;

    /**
     * Creates a property information instance.
     *
     * @param name name of the property being imported
     * @param type type of the property being imported
     * @param values value(s) of the property being imported
     */
    public PropInfo(Name name, int type, TextValue[] values) {
        this.name = name;
        this.type = type;
        this.values = values.clone();
    }

    /**
     * Disposes all values contained in this property.
     */
    public void dispose() {
        for (int i = 0; i < values.length; i++) {
            values[i].dispose();
        }
    }

    private int getTargetType(PropDef def) {
        int target = def.getRequiredType();
        if (target != PropertyType.UNDEFINED) {
            return target;
        } else if (type != PropertyType.UNDEFINED) {
            return type;
        } else {
            return PropertyType.STRING;
        }
    }

    private PropDef getApplicablePropertyDef(EffectiveNodeType ent)
            throws ConstraintViolationException {
        if (values.length == 1) {
            // could be single- or multi-valued (n == 1)
            return ent.getApplicablePropertyDef(name, type);
        } else {
            // can only be multi-valued (n == 0 || n > 1)
            return ent.getApplicablePropertyDef(name, type, true);
        }
    }

    public void apply(
            NodeImpl node, NamePathResolver resolver,
            Map<NodeId, Reference> derefNodes, String basePath, int referenceBehavior) throws RepositoryException {
        // find applicable definition
        PropDef def = getApplicablePropertyDef(node.getEffectiveNodeType());
        if (def.isProtected()) {
            // skip protected property
            log.debug("skipping protected property " + name);
            return;
        }

        // convert serialized values to Value objects
        Value[] va = new Value[values.length];
        int targetType = getTargetType(def);
        for (int i = 0; i < values.length; i++) {
            va[i] = values[i].getValue(targetType, resolver);
        }

        if (name.equals(Reference.PROPERTY_NAME)) {
            // always multiple, one entry per property
            for (int i = 0; i < values.length; i++) {

                // 0. add nodeId and Reference for later processing
                // 1. if prop != mandatory => don't set
                // 2. if prop == mandatory
                // 2.1 if prop is multi => set empty
                // 2.2 if prop is single => set ref to root
                
                Reference ref =  new Reference(va[i].getString());
                derefNodes.put(node.getNodeId(), ref);
                String targetPropName = ref.getPropertyName();
                
                if (node.getDefinition().getDeclaringNodeType().canRemoveItem(targetPropName)) {
                    // not mandatory
                    continue;
                }
                
                // best effort guess, original data multi value prop => new data multi value prop
                if (ref.isMulti()) {
                    // set empty
                    node.setProperty(targetPropName, new Value[] {});
                    continue;
                }
                
                // sigle value mandatory property, temporary set ref to rootNode
                Value rootRef = node.getSession().getValueFactory().createValue(node.getSession().getRootNode().getUUID(), PropertyType.REFERENCE);
                node.setProperty(targetPropName, rootRef);
            }
            return;
        }
        
        
        // multi- or single-valued property?
        if (va.length == 1 && !def.isMultiple()) {
            Exception e = null;
            try {
                // set single-value
                node.setProperty(name, va[0]);
            } catch (ValueFormatException vfe) {
                e = vfe;
            } catch (ConstraintViolationException cve) {
                e = cve;
            }
            if (e != null) {
                // setting single-value failed, try setting value array
                // as a last resort (in case there are ambiguous property
                // definitions)
                node.setProperty(name, va, type);
            }
        } else {
            // can only be multi-valued (n == 0 || n > 1)
            node.setProperty(name, va, type);
        }
    }

//    public void apply(
//            NodeState node, BatchedItemOperations itemOps, NodeTypeRegistry ntReg, 
//            List<NodeId> derefNodes, String basePath, int referenceBehavior)
//            throws RepositoryException {
//        PropertyState prop = null;
//        PropDef def = null;
//
//        if (node.hasPropertyName(name)) {
//            // a property with that name already exists...
//            PropertyId idExisting = new PropertyId(node.getNodeId(), name);
//            prop = (PropertyState) itemOps.getItemState(idExisting);
//            def = ntReg.getPropDef(prop.getDefinitionId());
//            if (def.isProtected()) {
//                // skip protected property
//                log.debug("skipping protected property "
//                        + itemOps.safeGetJCRPath(idExisting));
//                return;
//            }
//            if (!def.isAutoCreated()
//                    || (prop.getType() != type && type != PropertyType.UNDEFINED)
//                    || def.isMultiple() != prop.isMultiValued()) {
//                throw new ItemExistsException(itemOps.safeGetJCRPath(prop.getPropertyId()));
//            }
//        } else {
//            // there's no property with that name,
//            // find applicable definition
//            def = getApplicablePropertyDef(itemOps.getEffectiveNodeType(node));
//            if (def.isProtected()) {
//                // skip protected property
//                log.debug("skipping protected property " + name);
//                return;
//            }
//
//            // create new property
//            prop = itemOps.createPropertyState(node, name, type, def);
//        }
//
//        // check multi-valued characteristic
//        if (values.length != 1 && !def.isMultiple()) {
//            throw new ConstraintViolationException(itemOps.safeGetJCRPath(prop.getPropertyId())
//                    + " is not multi-valued");
//        }
//
//        // convert serialized values to InternalValue objects
//        int targetType = getTargetType(def);
//        InternalValue[] iva = new InternalValue[values.length];
//        for (int i = 0; i < values.length; i++) {
//            iva[i] = values[i].getInternalValue(targetType);
//        }
//
//        // set values
//        prop.setValues(iva);
//
//        // make sure property is valid according to its definition
//        itemOps.validate(prop);
//
//        if (name.equals(Reference.PROPERTY_NAME)) {
//            // store reference for later resolution
//            derefNodes.add(node.getNodeId());
//        }
//
//        // store property
//        itemOps.store(prop);
//    }
}
