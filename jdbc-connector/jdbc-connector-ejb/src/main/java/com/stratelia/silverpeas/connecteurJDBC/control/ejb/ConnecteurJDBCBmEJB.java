/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.connecteurJDBC.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;

import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail;
import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;

public class ConnecteurJDBCBmEJB implements ConnecteurJDBCBmBusinessSkeleton,
    SessionBean {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public Collection<ConnecteurJDBCConnectionInfoDetail> getConnectionList(ConnecteurJDBCConnectionInfoPK pk)
      throws RemoteException, PersistenceException {
    SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory
        .getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
    return dao.findByWhereClause(pk, "instanceId = " + "'"
        + pk.getComponentName() + "'");
  }

  public ConnecteurJDBCConnectionInfoDetail getConnection(
      ConnecteurJDBCConnectionInfoPK pk) throws RemoteException,
      PersistenceException {
    SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory
        .getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
    return (ConnecteurJDBCConnectionInfoDetail) dao.findByPrimaryKey(pk);
  }

  public ConnecteurJDBCConnectionInfoPK addConnection(
      ConnecteurJDBCConnectionInfoDetail detail) throws RemoteException,
      PersistenceException {
    SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory
        .getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
    return (ConnecteurJDBCConnectionInfoPK) dao.add(detail);
  }

  public void updateConnection(ConnecteurJDBCConnectionInfoDetail detail)
      throws RemoteException, PersistenceException {
    SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory
        .getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
    dao.update(detail);
  }

  public void removeConnection(ConnecteurJDBCConnectionInfoPK pk)
      throws RemoteException, PersistenceException {
    SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory
        .getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
    dao.remove(pk);
  }

  /**
   * ejb methods
   */
  public void ejbCreate() throws CreateException {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.ejbCreate()",
        "connecteurJDBC.MSG_ENTER");
  }

  public void ejbRemove() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.ejbRemove()",
        "connecteurJDBC.MSG_ENTER");
  }

  public void ejbActivate() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.ejbActivate()",
        "connecteurJDBC.MSG_ENTER");
  }

  public void ejbPassivate() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.ejbPassivate()",
        "connecteurJDBC.MSG_ENTER");
  }

  public void setSessionContext(final javax.ejb.SessionContext p1)
      throws javax.ejb.EJBException, java.rmi.RemoteException {
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCBmEJB.setSessionContext()", "connecteurJDBC.MSG_ENTER");
  }

}