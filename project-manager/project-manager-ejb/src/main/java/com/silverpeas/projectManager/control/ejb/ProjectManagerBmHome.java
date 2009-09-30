package com.silverpeas.projectManager.control.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface ProjectManagerBmHome extends EJBHome {
  public ProjectManagerBm create() throws RemoteException, CreateException;
}
