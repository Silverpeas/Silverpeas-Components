package com.stratelia.silverpeas.connecteurJDBC.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail;
import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoPK;
import com.stratelia.webactiv.persistence.PersistenceException;

public interface ConnecteurJDBCBmBusinessSkeleton {

  public Collection getConnectionList(ConnecteurJDBCConnectionInfoPK pk)
      throws RemoteException, PersistenceException;

  public void removeConnection(ConnecteurJDBCConnectionInfoPK pk)
      throws RemoteException, PersistenceException;

  public ConnecteurJDBCConnectionInfoDetail getConnection(
      ConnecteurJDBCConnectionInfoPK pk) throws RemoteException,
      PersistenceException;

  public ConnecteurJDBCConnectionInfoPK addConnection(
      ConnecteurJDBCConnectionInfoDetail detail) throws RemoteException,
      PersistenceException;

  public void updateConnection(ConnecteurJDBCConnectionInfoDetail detail)
      throws RemoteException, PersistenceException;
}