/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.testutils.history;

/**
 * XmlConstants.java
 */
public interface XmlConstants {

  // the testsuite element
  public final static String TESTSUITE = "testsuite";

  // classname attribute for testsuite elements
  public final static String ATTR_CLASSNAME = "classname";

  // the testcase element
  public final static String TESTCASE = "testcase";

  // name attribute for testcase and metric elements
  public final static String ATTR_NAME = "name";

  // the metric element
  public final static String METRIC = "metric";

  // unit attribute for metric elements
  public final static String ATTR_UNIT = "unit";

  // fuzzy attribute for metric elements
  public final static String ATTR_FUZZY = "fuzzy";

  // the measurepoint element
  public final static String MEASUREPOINT = "measurepoint";

  // timestamp attributes for measurepoint elements
  public final static String ATTR_TIMESTAMP = "timestamp";

  // value attribute for measurepoint elements
  public final static String ATTR_VALUE = "value";

  // skip attribute for measurepoint elements
  public final static String ATTR_SKIP = "skip";

}
