package com.stratelia.webactiv.yellowpages.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
/** 
 * The Home interface for YellowpagesBM EJB 
 */
public interface YellowpagesBmHome extends EJBHome {
    public YellowpagesBm create() throws RemoteException, CreateException;
}
