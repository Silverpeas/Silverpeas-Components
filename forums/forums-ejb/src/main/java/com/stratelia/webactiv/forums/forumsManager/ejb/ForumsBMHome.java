/*******************************
 *** ForumsBMHome            ***
 *** cree par Franck Rageade ***
 *** le 28 Septembre 2000    ***
 *******************************/

package com.stratelia.webactiv.forums.forumsManager.ejb;

// Bibliotheques
import javax.ejb.EJBHome;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

/**
 * Cette classe est l'interface Home du Business Manager qui gere les forums
 * 
 * @author frageade
 * @since September 2000
 */
public interface ForumsBMHome extends EJBHome {

  /**
   * Cree une instance d'un objet ForumsBM
   * 
   * @return ForumsBM l'instance cree
   * @see com.stratelia.forums.forumsManager.ejb.ForumsBM
   * @exception javax.ejb.RemoteException
   * @exception javax.ejb.CreateException
   * @author frageade
   * @since September 2000
   */
  ForumsBM create() throws RemoteException, CreateException;
}

/**********************
 *** Fin du fichier ***
 **********************/
