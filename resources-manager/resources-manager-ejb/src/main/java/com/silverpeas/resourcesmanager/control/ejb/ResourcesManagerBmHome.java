package com.silverpeas.resourcesmanager.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ResourcesManagerBmHome extends EJBHome {
    public ResourcesManagerBm create() throws RemoteException, CreateException;
}