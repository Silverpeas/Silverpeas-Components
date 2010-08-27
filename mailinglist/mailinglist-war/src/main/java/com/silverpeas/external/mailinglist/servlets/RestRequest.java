/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.external.mailinglist.servlets;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class RestRequest implements MailingListRoutage {
  public static final int UPDATE = 1;
  public static final int DELETE = 2;
  public static final int FIND = 3;
  public static final int CREATE = 4;
  private String componentId;
  private Map<String, String> elements;
  private int action;

  public RestRequest(HttpServletRequest request) {
    elements = new HashMap<String, String> (10);
    if ("POST".equalsIgnoreCase(request.getMethod())) {
      action = CREATE;
    } else if ("GET".equalsIgnoreCase(request.getMethod())) {
      action = FIND;
    } else if ("PUT".equalsIgnoreCase(request.getMethod())) {
      action = UPDATE;
    } else if ("DELETE".equalsIgnoreCase(request.getMethod())) {
      action = DELETE;
    }
    String pathInfo = request.getRequestURI();
    String context = request.getContextPath();
    int startIndex = pathInfo.indexOf(context) + context.length();
    pathInfo = pathInfo.substring(startIndex);
    // substring du context
    if (pathInfo.startsWith("/")) {
      pathInfo = pathInfo.substring(1);
    }
    SilverTrace.debug("mailingList", "RestRequest()",
        "root.MSG_GEN_ENTER_METHOD", "Parsing:" + pathInfo);
    StringTokenizer tokenizer = new StringTokenizer(pathInfo, "/", false);
    String element = tokenizer.nextToken();
    String id = tokenizer.nextToken();
    if ("RmailingList".equalsIgnoreCase(element)) {
      componentId = id;
      SilverTrace.debug("mailingList", "RestRequest()",
          "root.MSG_GEN_ENTER_METHOD", "componentId=" + id);
    } else if (id != null) {
      elements.put(element, id);
      SilverTrace.debug("mailingList", "RestRequest()",
          "root.MSG_GEN_ENTER_METHOD", element + '=' + id);
    }
    boolean isKey = true;
    String key = null;
    String value = null;
    while (tokenizer.hasMoreTokens()) {
      value = tokenizer.nextToken();
      if (isKey) {
        key = value;
        isKey = false;
      } else {
        elements.put(key, value);
        SilverTrace.debug("mailingList", "RestRequest()",
            "root.MSG_GEN_ENTER_METHOD", key + '=' + value);
        isKey = true;
      }
    }
    if (DELETE_ACTION.equalsIgnoreCase(value)) {
      this.action = DELETE;
    } else if (UPDATE_ACTION.equalsIgnoreCase(value)) {
      this.action = UPDATE;
    }
  }

  public Map<String, String>  getElements() {
    return elements;
  }

  public int getAction() {
    return action;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }

}
