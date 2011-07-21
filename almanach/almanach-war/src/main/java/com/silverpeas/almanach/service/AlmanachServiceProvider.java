/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.almanach.service;

import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachException;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;

/**
 * It is a provider of business services on the almanachs for the REST-based almanach web service.
 * It wraps the access to the underlying almanach business service bean by extending it with some
 * operations required by the web service; the services published by the web resource are delegated
 * to this object.
 */
@Named("almanachServiceProvider")
public class AlmanachServiceProvider {
  
  private AlmanachBm almanachBean;

  /**
   * Gets the next event occurrences that will occur in the specified almanach.
   * @param almanachId the unique identifier of the almanach instance.
   * @return a list of the next event occurrences.
   * @throws AlmanachException if an error occured when fetching the occurrences of its events.
   */
  public List<EventOccurrence> getNextEventOccurrencesOf(String almanachId) throws AlmanachException {
    try {
      return getAlmanachBean().getNextEventOccurrences(almanachId);
    } catch (RemoteException ex) {
      Logger.getLogger(AlmanachServiceProvider.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }
  
  /**
   * Gets the remote business bean for handling almanachs and events.
   * @return the remote business bean.
   * @throws AlmanachException if an error occurs while getting the remote object.
   */
  protected AlmanachBm getAlmanachBean() throws AlmanachException {
    if (almanachBean == null) {
      try {
        almanachBean = ((AlmanachBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class)).create();
      } catch (Exception e) {
        throw new AlmanachException("AlmanachSessionControl.getAlmanachBm()",
            SilverpeasException.ERROR, "almanach.EX_EJB_CREATION_FAIL", e);
      }
    }
    return almanachBean;
  }
  
  /**
   * Sets a specific reference to a remote Almanach business object.
   * Used mainly in tests.
   * @param anAlmanachBm the reference to a remote business object.
   */
  public void setAlmanachBean(final AlmanachBm anAlmanachBm) {
    this.almanachBean = anAlmanachBm;
  }
}
