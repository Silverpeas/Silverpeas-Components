package com.silverpeas.external.mailinglist.servlets;

public interface MailingListRoutage {
  public static final String COMPONENT_ID_ATT = "componentId";
  public static final String MESSAGE_ATT = "currentMessage";
  public static final String MESSAGE_ATTACHMENTS_ATT = "currentMessageAttachments";
  public static final String MESSAGES_LIST_ATT = "currentMessageList";
  public static final String ACTIVITY_LIST_ATT = "currentListActivity";
  public static final String ACTIVITY_MAP_ATT = "currentActivityMap";
  public static final String ACTIVITY_YEARS_ATT = "currentYears";
  public static final String MAILING_LIST_ATT = "currentList";
  public static final String NB_PAGE_ATT = "nbPages";
  public static final String CURRENT_PAGE_ATT = "currentPage";
  public static final String IS_USER_ADMIN_ATT = "currentUserIsAdmin";
  public static final String IS_USER_MODERATOR_ATT = "currentUserIsModerator";
  public static final String IS_USER_SUBSCRIBER_ATT = "currentUserIsSubscriber";
  public static final String IS_LIST_MODERATED_ATT = "currentListIsModerated";
  public static final String RSS_URL_ATT = "mailinglistRss";
  public static final String USERS_LIST_ATT = "currentUsersList";
  public static final String PREVIOUS_PATH_ATT = "currentFromPath";
  public static final String PORTLET_MODE_ATT = "portletMode";

  public static String JSP_BASE = "/mailingList/jsp/";

  public static final String DESTINATION_MODERATION = "moderationList";
  public static final String DESTINATION_MESSAGE = "message";
  public static final String DESTINATION_LIST = "list";
  public static final String DESTINATION_ACTIVITIES = "activities";
  public static final String DESTINATION_USERS = "users";
  public static final String DESTINATION_ATTACHMENT = "mailingListAttachment";
  public static final String DESTINATION_SUBSCRIBERS = "subscription";
  public static final String DESTINATION_PORTLET = "portlet";

  public static final String UPDATE_ACTION = "put";
  public static final String DELETE_ACTION = "delete";
  public static final String DESTINATION_ELEMENT = "destination";
  public static final String MODERATION_VALUE = "moderation";
  public static final String LIST_VALUE = "list";
  public static final String MESSAGE_VALUE = "message";
  public static final String USERS_VALUE = "users";
  public static final String SUBSCRIBERS_VALUE = "subscription";

  public static final String DESTINATION_DISPLAY_MODERATION = "moderation.jsp";
  public static final String DESTINATION_DISPLAY_MESSAGE = "message.jsp";
  public static final String DESTINATION_DISPLAY_LIST = "list.jsp";
  public static final String DESTINATION_DISPLAY_ACTIVITY = "activity.jsp";
  public static final String DESTINATION_DISPLAY_USERS = "users.jsp";
  public static final String DESTINATION_DISPLAY_PORTLET = "portlet.jsp";


  public static final String CURRENT_YEAR_PARAM = "currentYear";
  public static final String CURRENT_MONTH_PARAM = "currentMonth";
  public static final String CURRENT_PAGE_PARAM = "currentPage";
  public static final String ORDER_BY_PARAM = "orderBy";
  public static final String ORDER_ASC_PARAM = "ascendant";
  public static final String SELECTED_MESSAGE_PARAM = "message";
  public static final String SELECTED_USERS_PARAM = "users";
  public static final String USERS_LIST_PARAM = "users";

}
