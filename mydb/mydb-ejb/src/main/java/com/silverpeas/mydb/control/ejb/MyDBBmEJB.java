/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.mydb.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.mydb.model.MyDBConnectionInfoDetail;
import com.silverpeas.mydb.model.MyDBConnectionInfoPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;

/**
 * MyDB EJB-tier controller of the MVC.
 * @author Antoine HEDIN
 */
public class MyDBBmEJB implements MyDBBmBusinessSkeleton, SessionBean {

  private static final long serialVersionUID = -3635064500545742524L;
  private static final String BEAN_NAME = MyDBConnectionInfoDetail.class.getName();

  @Override
  public Collection<MyDBConnectionInfoDetail> getConnectionList(MyDBConnectionInfoPK pk)
      throws RemoteException, PersistenceException {
    return getMyDBConnectionDao().findByWhereClause(pk,
        "instanceId = " + "'" + pk.getComponentName() + "'");
  }

  @Override
  public MyDBConnectionInfoDetail getConnection(MyDBConnectionInfoPK pk) throws RemoteException,
      PersistenceException {
    return getMyDBConnectionDao().findByPrimaryKey(pk);
  }

  @Override
  public MyDBConnectionInfoPK addConnection(MyDBConnectionInfoDetail detail) throws RemoteException,
      PersistenceException {
    return (MyDBConnectionInfoPK) getMyDBConnectionDao().add(detail);
  }

  @Override
  public void updateConnection(MyDBConnectionInfoDetail detail) throws RemoteException,
      PersistenceException {
    getMyDBConnectionDao().update(detail);
  }

  @Override
  public void removeConnection(MyDBConnectionInfoPK pk) throws RemoteException,
      PersistenceException {
     getMyDBConnectionDao().remove(pk);
  }

  public void ejbCreate() throws CreateException {
    SilverTrace.info("myDB", "MyDBBmEJB.ejbCreate()", "myDB.MSG_ENTER");
  }

  @Override
  public void setSessionContext(final SessionContext p1) throws EJBException, RemoteException {
    SilverTrace.info("myDB", "MyDBBmEJB.setSessionContext()", "myDB.MSG_ENTER");
  }

  @Override
  public void ejbRemove() throws EJBException, RemoteException {
    SilverTrace.info("myDB", "MyDBBmEJB.ejbRemove()", "myDB.MSG_ENTER");
  }

  @Override
  public void ejbActivate() throws EJBException, RemoteException {
    SilverTrace.info("myDB", "MyDBBmEJB.ejbActivate()", "myDB.MSG_ENTER");
  }

  @Override
  public void ejbPassivate() throws EJBException, RemoteException {
    SilverTrace.info("myDB", "MyDBBmEJB.ejbPassivate()", "myDB.MSG_ENTER");
  }

  private SilverpeasBeanDAO<MyDBConnectionInfoDetail> getMyDBConnectionDao() throws
      PersistenceException {
    return SilverpeasBeanDAOFactory.<MyDBConnectionInfoDetail>getDAO(BEAN_NAME);
  }
}