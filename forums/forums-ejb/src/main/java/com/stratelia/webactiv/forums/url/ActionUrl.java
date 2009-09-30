package com.stratelia.webactiv.forums.url;

public class ActionUrl {
  private static final String[] FIELD_NAMES = { "Space", "Component", "call",
      "action", "params", "forumId", "addStat", "changeDisplay" };
  private static final String[] FIELD_DEF_VALUES = { "", "", "", "-1", "-1",
      "-1", "false", "false" };
  private static final int FIELD_COUNT = FIELD_NAMES.length;

  private static final String DEFAULT_PAGE = "main";

  private String spaceId = "";
  private String componentId = "";
  private String page = null;
  private String call = "";
  private int action = -1;
  private int params = -1;
  private int forumId = -1;
  private boolean addStat = false;
  private boolean changeDisplay = false;

  public static String getUrl(String page) {
    return (new ActionUrl(page)).toString();
  }

  public static String getUrl(String page, int action, int forumId) {
    return (new ActionUrl(page, action, forumId)).toString();
  }

  public static String getUrl(String page, int action, int params, int forumId) {
    return (new ActionUrl(page, action, params, forumId)).toString();
  }

  public static String getUrl(String page, String call, int forumId) {
    return (new ActionUrl(page, call, forumId)).toString();
  }

  public static String getUrl(String page, String call, int action, int params) {
    return (new ActionUrl(page, call, action, params)).toString();
  }

  public static String getUrl(String page, String call, int action, int params,
      int forumId) {
    return (new ActionUrl(page, call, action, params, forumId)).toString();
  }

  public static String getUrl(String page, String call, int action, int params,
      int forumId, boolean addStat, boolean changeDisplay) {
    return (new ActionUrl(page, call, action, params, forumId, addStat,
        changeDisplay)).toString();
  }

  public static String getUrl(String spaceId, String componentId, String page,
      String call, int action, int params, int forumId) {
    return (new ActionUrl(spaceId, componentId, page, call, action, params,
        forumId)).toString();
  }

  private ActionUrl(String page) {
    this.page = page;
  }

  private ActionUrl(String page, int action, int forumId) {
    this.page = page;
    this.action = action;
    this.forumId = forumId;
  }

  private ActionUrl(String page, int action, int params, int forumId) {
    this.page = page;
    this.action = action;
    this.params = params;
    this.forumId = forumId;
  }

  private ActionUrl(String page, String call, int forumId) {
    this.page = page;
    this.call = call;
    this.forumId = forumId;
  }

  private ActionUrl(String page, String call, int action, int params) {
    this.page = page;
    this.call = call;
    this.action = action;
    this.params = params;
  }

  private ActionUrl(String page, String call, int action, int params,
      int forumId) {
    this.page = page;
    this.call = call;
    this.action = action;
    this.params = params;
    this.forumId = forumId;
  }

  private ActionUrl(String page, String call, int action, int params,
      int forumId, boolean addStat, boolean changeDisplay) {
    this.page = page;
    this.call = call;
    this.action = action;
    this.params = params;
    this.forumId = forumId;
    this.addStat = addStat;
    this.changeDisplay = changeDisplay;
  }

  private ActionUrl(String spaceId, String componentId, String page,
      String call, int action, int params, int forumId) {
    this.spaceId = spaceId;
    this.componentId = componentId;
    this.page = page;
    this.call = call;
    this.action = action;
    this.params = params;
    this.forumId = forumId;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    String[] values = { spaceId, componentId, call, String.valueOf(action),
        String.valueOf(params), String.valueOf(forumId),
        String.valueOf(addStat), String.valueOf(changeDisplay) };
    for (int i = 0; i < FIELD_COUNT; i++) {
      if (!values[i].equals(FIELD_DEF_VALUES[i])) {
        sb.append(sb.length() > 0 ? "&" : "?").append(FIELD_NAMES[i]).append(
            "=").append(values[i]);
      }
    }
    return ((page == null || page.length() == 0) ? DEFAULT_PAGE : page)
        + ".jsp" + sb.toString();
  }

}