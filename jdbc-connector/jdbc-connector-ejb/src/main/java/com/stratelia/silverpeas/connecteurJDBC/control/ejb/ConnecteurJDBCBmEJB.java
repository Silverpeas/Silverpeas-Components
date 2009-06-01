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


public class ConnecteurJDBCBmEJB implements ConnecteurJDBCBmBusinessSkeleton, SessionBean {

  public Collection getConnectionList(ConnecteurJDBCConnectionInfoPK pk)
	throws RemoteException, PersistenceException
  {
	  SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
	  return dao.findByWhereClause(pk, "instanceId = "+"'"+pk.getComponentName()+"'");
  }

  public ConnecteurJDBCConnectionInfoDetail getConnection(ConnecteurJDBCConnectionInfoPK pk)
	throws RemoteException, PersistenceException
  {
	  SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
	  return (ConnecteurJDBCConnectionInfoDetail) dao.findByPrimaryKey(pk);
  }

  public ConnecteurJDBCConnectionInfoPK addConnection(ConnecteurJDBCConnectionInfoDetail detail)
	throws RemoteException, PersistenceException
  {
	  SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
	  return (ConnecteurJDBCConnectionInfoPK) dao.add(detail);
  }

  public void updateConnection(ConnecteurJDBCConnectionInfoDetail detail)
	throws RemoteException, PersistenceException
  {
	  SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
	  dao.update(detail);
  }

  public void removeConnection(ConnecteurJDBCConnectionInfoPK pk)
	throws RemoteException, PersistenceException
  {
	  SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO("com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail");
	  dao.remove(pk);
  }



  /**
   * ejb methods
   */
  public void ejbCreate() throws CreateException
  {
	SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.ejbCreate()", "connecteurJDBC.MSG_ENTER");
  }

  public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException
  {
	SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.ejbRemove()", "connecteurJDBC.MSG_ENTER");
  }

  public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException
  {
	SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.ejbActivate()", "connecteurJDBC.MSG_ENTER");
  }
  public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException
  {
	SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.ejbPassivate()", "connecteurJDBC.MSG_ENTER");
  }

  public void setSessionContext(final javax.ejb.SessionContext p1) throws javax.ejb.EJBException, java.rmi.RemoteException
  {
	SilverTrace.info("connecteurJDBC", "ConnecteurJDBCBmEJB.setSessionContext()", "connecteurJDBC.MSG_ENTER");
  }

}