/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.kmelia.control.ejb;

import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import javax.inject.Inject;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaServiceProvider {

  private static final KmeliaServiceProvider instance = new KmeliaServiceProvider();

  @Inject
  private KmeliaBm kmeliaBm;

  /**
   * @return an instance of {@link KmeliaBm} EJB.
   */
  public static KmeliaBm getKmeliaService() {
    if (getInstance().kmeliaBm == null) {
      try {
        // If not initialize, then the EJB reference is searched by common tools.
        // By this way, the instance of this EJB is not stored.
        return EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaServiceProvider.getKmeliaService()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return getInstance().kmeliaBm;
  }

  /**
   * @return a RatingServiceFactory instance.
   */
  public static KmeliaServiceProvider getInstance() {
    return instance;
  }
}
