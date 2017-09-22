/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.taxonomy.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang.LocaleUtils;

public class TaxonomyUtil {

    /**
     * Builds a list of Locales from Strings.
     * @param localeStrings {@link #toLocale}
     * @return a list of Locales
     */
    public static List<Locale> getLocalesList(final String[] localeStrings) {
        if (localeStrings == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(localeStrings).map(TaxonomyUtil::toLocale).collect(Collectors.toList());
    }

    /**
     * Creates a Locale from a String.
     * @param localeString can be in Java Locale#toString format or a LanguageTag as described by the IETF BCP 47
     *                     specification. For example "en_GB" and "en-GB" will result in the same Locale object.
     * @return null if localeString was null, or the requested Locale
     */
    public static Locale toLocale(final String localeString) {
        if (localeString == null) {
            return null;
        }
        if (localeString.contains("_")) {
            return LocaleUtils.toLocale(localeString);
        } else {
            return Locale.forLanguageTag(localeString);
        }
    }

    private TaxonomyUtil () {}

}
