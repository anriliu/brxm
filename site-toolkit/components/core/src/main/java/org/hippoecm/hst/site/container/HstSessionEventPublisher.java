/*
 *  Copyright 2012 Hippo.
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
package org.hippoecm.hst.site.container;

import java.util.Map;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.hippoecm.hst.site.HstServices;

public class HstSessionEventPublisher implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        Map<String, HttpSessionListener> listeners = HstServices.getComponentManager().getComponentsOfType(
                                        HttpSessionListener.class);
        for (Map.Entry<String, HttpSessionListener> entry : listeners.entrySet()) {
            entry.getValue().sessionCreated(event);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        Map<String, HttpSessionListener> listeners = HstServices.getComponentManager().getComponentsOfType(
                                        HttpSessionListener.class);
        for (Map.Entry<String, HttpSessionListener> entry : listeners.entrySet()) {
            entry.getValue().sessionDestroyed(event);
        }
    }

}
