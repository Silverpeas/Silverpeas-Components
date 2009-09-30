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
  private Map elements;
  private int action;

  public RestRequest(HttpServletRequest request) {
    elements = new HashMap(10);
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

  public Map getElements() {
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
