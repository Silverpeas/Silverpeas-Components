package com.silverpeas.mailinglist;

import java.sql.Connection;

import com.silverpeas.mailinglist.model.MailingListComponent;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

public class MailingListInstanciator implements ComponentsInstanciatorIntf {

  public MailingListInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("mailingList", "MailingListInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
            + componentId + ", userId =" + userId);
    MailingList mailingList = new MailingList();
    mailingList.setComponentId(componentId);
    SilverTrace.info("mailingList", "MailingListInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
    ServicesFactory.getMailingListService().createMailingList(mailingList);
    SilverTrace.info("mailingList", "MailingListInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
    mailingList = ServicesFactory.getMailingListService().findMailingList(componentId);
    MailingListComponent component = new MailingListComponent(componentId);
    ServicesFactory.getMessageChecker().addMessageListener(component);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("mailingList", "MailingListInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
            + componentId + ", userId =" + userId);
    // insert your code here !
    ServicesFactory.getMailingListService().deleteMailingList(componentId);
    SilverTrace.info("mailingList", "MailingListInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
    ServicesFactory.getMessageChecker().removeListener(componentId);
  }
}