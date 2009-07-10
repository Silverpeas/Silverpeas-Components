package com.silverpeas.processManager.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface ProcessManagerBm extends EJBObject {

	public String createProcess(String componentId, String userId, String fileName, byte[] fileContent) throws RemoteException;
}
