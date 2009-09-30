/*
 * HyperlinkInstanciator.java
 *
 * Created on 22 juin 2001, 09:54
 */

package com.stratelia.webactiv.hyperlink;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

/**
 * 
 * @author nchaix
 * @version
 */
public class HyperlinkInstanciator extends Object implements
    ComponentsInstanciatorIntf {

  /** Creates new HyperlinkInstanciator */
  public HyperlinkInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("hyperlink", "HyperlinkInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "space = " + spaceId);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("hyperlink", "HyperlinkInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "space = " + spaceId);
  }
}