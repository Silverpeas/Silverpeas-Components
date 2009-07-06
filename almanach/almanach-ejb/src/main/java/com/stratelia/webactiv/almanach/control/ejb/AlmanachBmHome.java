package com.stratelia.webactiv.almanach.control.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface AlmanachBmHome extends EJBHome
{
  
  public AlmanachBm create() throws RemoteException, CreateException;
  
}