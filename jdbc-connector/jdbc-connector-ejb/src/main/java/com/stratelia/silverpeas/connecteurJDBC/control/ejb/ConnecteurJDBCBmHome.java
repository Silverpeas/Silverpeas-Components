package com.stratelia.silverpeas.connecteurJDBC.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ConnecteurJDBCBmHome extends EJBHome {

  public ConnecteurJDBCBm create() throws RemoteException, CreateException;

}