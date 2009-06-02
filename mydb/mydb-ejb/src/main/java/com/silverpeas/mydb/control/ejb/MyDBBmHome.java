package com.silverpeas.mydb.control.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * The Home interface for MyDBBm EJB.
 * 
 * @author Antoine HEDIN
 */
public interface MyDBBmHome
	extends EJBHome
{
	
    public MyDBBm create() throws RemoteException, CreateException;
    
}