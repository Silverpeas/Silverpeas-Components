/*
 * chatInstanciator.java
 * @author  PHiL
 * Created on 12 février 2002, 10:30
 */

package com.stratelia.silverpeas.chat;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

/**
 * 
 * @author PHiL
 * @version
 */
public class ChatInstanciator extends Object implements
    ComponentsInstanciatorIntf {
  public ChatInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("chat", "ChatInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId
            + " , componentId = " + componentId);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {

    SilverTrace.info("chat", "ChatInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId
            + " , componentId = " + componentId);

    ChatDataAccess chatDAO = new ChatDataAccess(componentId);

    try {
      chatDAO.DeleteChatInstance();
    } catch (Exception e) {
      SilverTrace.info("chat", "ChatInstanciator.delete()",
          "root.EX_RECORD_DELETE_FAILED", "componentId " + componentId);
    }

  }

}
