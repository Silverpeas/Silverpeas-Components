/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.quickinfo;

import java.sql.Connection;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.thumbnail.ThumbnailInstanciator;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.publication.PublicationInstanciator;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;
import com.stratelia.webactiv.statistic.control.StatisticBm;

public class QuickInfoInstanciator extends Object implements ComponentsInstanciatorIntf {

  /** Creates new NewsInstanciator */
  public QuickInfoInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.debug("quickinfo", "QuickInfoInstanciator.create()",
        "QuickInfoInstanciator.create called with: space=" + spaceId);
    PublicationInstanciator pub = new PublicationInstanciator("com.stratelia.webactiv.quickinfo");
    pub.create(con, spaceId, componentId, userId);
    SilverTrace.debug("quickinfo", "QuickInfoInstanciator.create()",
        "QuickInfoInstanciator.create finished");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.debug("quickinfo", "QuickInfoInstanciator.delete()",
        "delete called with: space=" + spaceId);
    PublicationInstanciator pub = new PublicationInstanciator("com.stratelia.webactiv.quickinfo");
    pub.delete(con, spaceId, componentId, userId);
    
    // deleting thumbnails
    ThumbnailInstanciator thumbnails = new ThumbnailInstanciator();
    thumbnails.delete(con, spaceId, componentId, userId);
    
    // deleting comments
    CommentServiceFactory.getFactory().getCommentService()
        .deleteAllCommentsByComponentInstanceId(componentId);
    
    // deleting stats
    getStatisticService().deleteStatsOfComponent(componentId);
    
    SilverTrace.debug("quickinfo", "QuickInfoInstanciator.delete()",
        "QuickInfoInstanciator.delete finished");
  }
  
  private StatisticBm getStatisticService() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBm.class);
  }
}