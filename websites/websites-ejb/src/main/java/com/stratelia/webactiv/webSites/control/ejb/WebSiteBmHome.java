/*
 * WebSiteBmHome.java
 *
 * Created on 9 Avril 2001, 13:00
 */

package com.stratelia.webactiv.webSites.control.ejb;

/** 
 * This is the WebSite manager EJB-tier controller of the MVC.
 * It is implemented as a session EJB. It controls all the activities 
 * that happen in a client session.
 * It also provides mechanisms to access other session EJBs.
 * @author Cécile BONIN
 * @version 1.0
 */

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * The Home interface for WebSiteBm EJB
 */
public interface WebSiteBmHome extends EJBHome {

  public WebSiteBm create() throws RemoteException, CreateException;
}
