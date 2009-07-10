package com.silverpeas.processManager.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ProcessManagerBmHome extends EJBHome {

	ProcessManagerBm create() throws RemoteException, CreateException;
}