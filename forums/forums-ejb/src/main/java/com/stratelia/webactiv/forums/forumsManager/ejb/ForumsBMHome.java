/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
 * @author frageade
 * @since September 2000
 */
public interface ForumsBMHome extends EJBHome {

  /**
   * Cree une instance d'un objet ForumsBM
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
