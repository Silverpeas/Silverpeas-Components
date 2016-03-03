/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist;

import com.silverpeas.admin.components.ComponentInstancePostConstruction;
import org.silverpeas.components.mailinglist.model.MailingListComponent;
import org.silverpeas.components.mailinglist.service.MailingListServicesProvider;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Registers for the spawned MailingList instance a message checkers to check and fetch new
 * incoming messages.
 * @author mmoquillon
 */
@Named
public class MailinglistInstancePostConstruction implements ComponentInstancePostConstruction {

  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    MailingList mailingList = new MailingList();
    mailingList.setComponentId(componentInstanceId);

    MailingListServicesProvider.getMailingListService().createMailingList(mailingList);

    MailingListComponent component = new MailingListComponent(componentInstanceId);
    MailingListServicesProvider.getMessageChecker().addMessageListener(component);
  }
}
