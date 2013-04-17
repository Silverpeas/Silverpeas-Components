/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.mailinglist.model.MailingListComponent;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.sql.Connection;

public class MailingListInstanciator implements ComponentsInstanciatorIntf {

  public MailingListInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("mailingList", "MailingListInstanciator.create()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);
    MailingList mailingList = new MailingList();
    mailingList.setComponentId(componentId);
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    SilverTrace.info("mailingList", "MailingListInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
    servicesFactory.getMailingListService().createMailingList(mailingList);
    SilverTrace.info("mailingList", "MailingListInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
    //mailingList = servicesFactory.getMailingListService().findMailingList(componentId);
    MailingListComponent component = new MailingListComponent(componentId);
    servicesFactory.getMessageChecker().addMessageListener(component);
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("mailingList", "MailingListInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    servicesFactory.getMailingListService().deleteMailingList(componentId);
    SilverTrace.info("mailingList", "MailingListInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
    servicesFactory.getMessageChecker().removeListener(componentId);
  }
}
