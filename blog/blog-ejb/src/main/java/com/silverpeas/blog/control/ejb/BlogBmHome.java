package com.silverpeas.blog.control.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface BlogBmHome extends EJBHome {
    public BlogBm create() throws RemoteException, CreateException;
}
