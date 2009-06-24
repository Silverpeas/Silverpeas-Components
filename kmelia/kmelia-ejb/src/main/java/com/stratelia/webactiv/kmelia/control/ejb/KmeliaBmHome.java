/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.kmelia.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * The Home interface for KMeliaBM EJB
 */
public interface KmeliaBmHome extends EJBHome
{

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws CreateException
     * @throws RemoteException
     *
     * @see
     */
    public KmeliaBm create() throws RemoteException, CreateException;
}
