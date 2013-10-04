/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mailinglist.service.job;

import java.util.Collection;
import java.util.List;

import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;

public class StubMailingListService implements MailingListService {

  public void addExternalUser(String componentId, ExternalUser user) {
    // TODO Auto-generated method stub

  }

  public void addExternalUsers(String componentId, Collection users) {
    // TODO Auto-generated method stub

  }

  public String createMailingList(MailingList mailingList) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deleteMailingList(String componentId) {
    // TODO Auto-generated method stub

  }

  public MailingList findMailingList(String componentId) {
    MailingList list = new MailingList();
    list.setComponentId(componentId);
    list.setSubscribedAddress(componentId);
    return list;
  }

  public List listAllMailingLists() {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeExternalUser(String componentId, ExternalUser user) {
    // TODO Auto-generated method stub

  }

  public void removeExternalUsers(String componentId, Collection users) {
    // TODO Auto-generated method stub

  }

  public void setGroupSubscribers(String componentId, Collection groups) {
    // TODO Auto-generated method stub

  }

  public void setInternalSubscribers(String componentId, Collection user) {
    // TODO Auto-generated method stub

  }

  public void subscribe(String componentId, String userId) {
    // TODO Auto-generated method stub

  }

  public void unsubscribe(String componentId, String userId) {
    // TODO Auto-generated method stub

  }

}
