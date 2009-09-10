package com.silverpeas.gallery.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface GalleryBmHome extends EJBHome {
    public GalleryBm create() throws RemoteException, CreateException;
}
