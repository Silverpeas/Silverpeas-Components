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
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;

/**
 * MyDB EJB-tier controller of the MVC.
 * 
 * @author Antoine HEDIN
 */
public class MyDBBmEJB implements MyDBBmBusinessSkeleton, SessionBean {

  private static final String BEAN_NAME = MyDBConnectionInfoDetail.class
      .getName();

  public Collection getConnectionList(MyDBConnectionInfoPK pk)
      throws RemoteException, PersistenceException {
    return SilverpeasBeanDAOFactory.getDAO(BEAN_NAME).findByWhereClause(pk,
        "instanceId = " + "'" + pk.getComponentName() + "'");
  }

  public MyDBConnectionInfoDetail getConnection(MyDBConnectionInfoPK pk)
      throws RemoteException, PersistenceException {
    return (MyDBConnectionInfoDetail) SilverpeasBeanDAOFactory
        .getDAO(BEAN_NAME).findByPrimaryKey(pk);
  }

  public MyDBConnectionInfoPK addConnection(MyDBConnectionInfoDetail detail)
      throws RemoteException, PersistenceException {
    return (MyDBConnectionInfoPK) SilverpeasBeanDAOFactory.getDAO(BEAN_NAME)
        .add(detail);
  }

  public void updateConnection(MyDBConnectionInfoDetail detail)
      throws RemoteException, PersistenceException {
    SilverpeasBeanDAOFactory.getDAO(BEAN_NAME).update(detail);
  }

  public void removeConnection(MyDBConnectionInfoPK pk) throws RemoteException,
      PersistenceException {
    SilverpeasBeanDAOFactory.getDAO(BEAN_NAME).remove(pk);
  }

  public void ejbCreate() throws CreateException {
    SilverTrace.info("myDB", "MyDBBmEJB.ejbCreate()", "myDB.MSG_ENTER");
  }

  public void setSessionContext(final SessionContext p1) throws EJBException,
      RemoteException {
    SilverTrace.info("myDB", "MyDBBmEJB.setSessionContext()", "myDB.MSG_ENTER");
  }

  public void ejbRemove() throws EJBException, RemoteException {
    SilverTrace.info("myDB", "MyDBBmEJB.ejbRemove()", "myDB.MSG_ENTER");
  }

  public void ejbActivate() throws EJBException, RemoteException {
    SilverTrace.info("myDB", "MyDBBmEJB.ejbActivate()", "myDB.MSG_ENTER");
  }

  public void ejbPassivate() throws EJBException, RemoteException {
    SilverTrace.info("myDB", "MyDBBmEJB.ejbPassivate()", "myDB.MSG_ENTER");
  }

}