package com.silverpeas.mydb.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import com.silverpeas.mydb.model.MyDBConnectionInfoDetail;
import com.silverpeas.mydb.model.MyDBConnectionInfoPK;
import com.stratelia.webactiv.persistence.PersistenceException;

/**
 * Interface extended by MyDBBm and implemented by MyDBBmEJB.
 * 
 * @author Antoine HEDIN
 */
public interface MyDBBmBusinessSkeleton {

  public Collection getConnectionList(MyDBConnectionInfoPK pk)
      throws RemoteException, PersistenceException;

  public void removeConnection(MyDBConnectionInfoPK pk) throws RemoteException,
      PersistenceException;

  public MyDBConnectionInfoDetail getConnection(MyDBConnectionInfoPK pk)
      throws RemoteException, PersistenceException;

  public MyDBConnectionInfoPK addConnection(MyDBConnectionInfoDetail detail)
      throws RemoteException, PersistenceException;

  public void updateConnection(MyDBConnectionInfoDetail detail)
      throws RemoteException, PersistenceException;

}
