/*
 * NewsInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.quickinfo;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.publication.PublicationInstanciator;

/**
 * 
 * @author squere
 * @version update by the Sébastien Antonio - Externalisation of the SQL request
 */
public class QuickInfoInstanciator extends Object implements
    ComponentsInstanciatorIntf {
  /** Creates new NewsInstanciator */
  public QuickInfoInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.debug("quickinfo", "QuickInfoInstanciator.create()",
        "QuickInfoInstanciator.create called with: space=" + spaceId);

    // create publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.quickinfo");
    pub.create(con, spaceId, componentId, userId);

    SilverTrace.debug("quickinfo", "QuickInfoInstanciator.create()",
        "QuickInfoInstanciator.create finished");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.debug("quickinfo", "QuickInfoInstanciator.delete()",
        "delete called with: space=" + spaceId);

    // delete publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.quickinfo");
    pub.delete(con, spaceId, componentId, userId);

    SilverTrace.debug("quickinfo", "QuickInfoInstanciator.delete()",
        "QuickInfoInstanciator.delete finished");
  }

}