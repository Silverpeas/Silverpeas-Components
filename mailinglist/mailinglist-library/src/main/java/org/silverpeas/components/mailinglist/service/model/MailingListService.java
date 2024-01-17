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

package org.silverpeas.components.mailinglist.service.model;

import java.util.Collection;
import java.util.List;

import org.silverpeas.components.mailinglist.service.model.beans.ExternalUser;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.core.util.ServiceProvider;

public interface MailingListService {

  public static final String PARAM_RSS = "rss";
  public static final String PARAM_OPEN = "open";
  public static final String PARAM_NOTIFY = "notify";
  public static final String PARAM_MODERATE = "moderated";
  public static final String PARAM_ADDRESS = "subscribedAddress";
  public static final String ROLE_MODERATOR = "moderator";
  public static final String ROLE_ADMINISTRATOR = "admin";
  public static final String ROLE_READER = "reader";
  public static final String ROLE_SUBSCRIBER = "subscriber";

  static MailingListService get() {
    return ServiceProvider.getService(MailingListService.class);
  }

  public String createMailingList(MailingList mailingList);

  public void addExternalUser(String componentId, ExternalUser user);

  public void addExternalUsers(String componentId, Collection<ExternalUser> users);

  public void removeExternalUser(String componentId, ExternalUser user);

  public void removeExternalUsers(String componentId, Collection<ExternalUser> users);

  public void setInternalSubscribers(String componentId, Collection<String> userIds);

  public void setGroupSubscribers(String componentId, Collection<String> groups);

  public void deleteMailingList(String componentId);

  public MailingList findMailingList(String componentId);

  public List<MailingList> listAllMailingLists();

  public void subscribe(String componentId, String userId);

  public void unsubscribe(String componentId, String userId);

}