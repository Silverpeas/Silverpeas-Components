/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package com.stratelia.webactiv.kmelia.control;

import com.silverpeas.comment.service.CommentServiceProvider;
import com.silverpeas.comment.service.CommentUserNotificationService;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

/**
 * Registers the Kmelia component as a source of notifications on the comments for the services
 * interested by this.
 *
 * Currently, only the CommentUserNotificationService is interested by such source of notification
 * on the comments.
 *
 * @author mmoquillon
 */
public class CommentNotificationSourceRegister {

  private static final String COMPONENT_NAME = "kmelia";

  /**
   * Performs the registration of the Kmelia component as a source of notification on the comments
   * with a CommentUserNotificationService instance.
   */
  public void initialize() {
    CommentUserNotificationService service = CommentServiceProvider.
        getCommentUserNotificationService();
    service.register(COMPONENT_NAME, getKmeliaService());
  }

  private KmeliaBm getKmeliaService() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaPublication.getKmeliaService()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_KmeliaBm_HOME", e);
    }
  }
}
