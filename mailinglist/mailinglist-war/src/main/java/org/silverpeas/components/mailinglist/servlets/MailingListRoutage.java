/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.mailinglist.servlets;

public interface MailingListRoutage {
  String COMPONENT_ID_ATT = "componentId";
  String MESSAGE_ATT = "currentMessage";
  String MESSAGE_ATTACHMENTS_ATT = "currentMessageAttachments";
  String MESSAGES_LIST_ATT = "currentMessageList";
  String ACTIVITY_LIST_ATT = "currentListActivity";
  String ACTIVITY_MAP_ATT = "currentActivityMap";
  String ACTIVITY_YEARS_ATT = "currentYears";
  String MAILING_LIST_ATT = "currentList";
  String NB_PAGE_ATT = "nbPages";
  String CURRENT_PAGE_ATT = "currentPage";
  String IS_USER_ADMIN_ATT = "currentUserIsAdmin";
  String IS_USER_MODERATOR_ATT = "currentUserIsModerator";
  String IS_USER_SUBSCRIBER_ATT = "currentUserIsSubscriber";
  String IS_LIST_MODERATED_ATT = "currentListIsModerated";
  String RSS_URL_ATT = "mailinglistRss";
  String USERS_LIST_ATT = "currentUsersList";
  String PREVIOUS_PATH_ATT = "currentFromPath";
  String PORTLET_MODE_ATT = "portletMode";

  String JSP_BASE = "/mailingList/jsp/";

  String DESTINATION_MODERATION = "moderationList";
  String DESTINATION_MESSAGE = "message";
  String DESTINATION_LIST = "list";
  String DESTINATION_ACTIVITIES = "activities";
  String DESTINATION_USERS = "users";
  String DESTINATION_ATTACHMENT = "mailingListAttachment";
  String DESTINATION_SUBSCRIBERS = "subscription";
  String DESTINATION_PORTLET = "portlet";

  String UPDATE_ACTION = "put";
  String DELETE_ACTION = "delete";
  String DESTINATION_ELEMENT = "destination";
  String MODERATION_VALUE = "moderation";
  String LIST_VALUE = "list";
  String MESSAGE_VALUE = "message";
  String USERS_VALUE = "users";
  String SUBSCRIBERS_VALUE = "subscription";

  String DESTINATION_DISPLAY_MODERATION = "moderation.jsp";
  String DESTINATION_DISPLAY_MESSAGE = "message.jsp";
  String DESTINATION_DISPLAY_LIST = "list.jsp";
  String DESTINATION_DISPLAY_ACTIVITY = "activity.jsp";
  String DESTINATION_DISPLAY_USERS = "users.jsp";
  String DESTINATION_DISPLAY_PORTLET = "portlet.jsp";

  String CURRENT_YEAR_PARAM = "currentYear";
  String CURRENT_MONTH_PARAM = "currentMonth";
  String CURRENT_PAGE_PARAM = "currentPage";
  String ORDER_BY_PARAM = "orderBy";
  String ORDER_ASC_PARAM = "ascendant";
  String SELECTED_MESSAGE_PARAM = "message";
  String SELECTED_USERS_PARAM = "users";
  String USERS_LIST_PARAM = "users";

}
