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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.forums.instanciator;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.node.model.NodeDetail;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;

import javax.ejb.EJBException;
import java.sql.Connection;
import java.util.Collection;

public class ForumsInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  /**
   * Le Business Manager
   */
  private ForumsBM forumsBM;

  /**
   * Creates new ForumsInstanciator
   */
  public ForumsInstanciator() {
    super("com.stratelia.webactiv.forums");
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    SilverTrace
        .info("forums", "ForumsInstanciator.create()", "forums.MSG_CREATE_WITH_SPACE_AND_COMPONENT",
            "space : " + spaceId + "component : " + componentId);
  }

  /**
   * Delete some rows of an instance of a forum.
   * @param con (Connection) the connection to the data base
   * @param spaceId (String) the id of a the space where the component exist.
   * @param componentId (String) the instance id of the Silverpeas component forum.
   * @param userId (String) the owner of the component
   */
  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    SilverTrace.info("forums", "ForumsInstanciator.delete()", "forums.MSG_DELETE_WITH_SPACE",
        "spaceId : " + spaceId);

    // Forums
    final Collection<Forum> forumRoots = getForumsBM().getForumRootList(componentId);
    for (Forum forum : forumRoots) {
      getForumsBM().deleteForum(forum.getPk());
    }

    // Categories
    for (NodeDetail categoryId : getForumsBM().getAllCategories(componentId)) {
      getForumsBM().deleteCategory(String.valueOf(categoryId.getId()), componentId);
    }

    // Unsubscribe component subscribers
    SubscriptionServiceProvider.getSubscribeService()
        .unsubscribeByResource(ComponentSubscriptionResource.from(componentId));
  }

  /**
   * Gets the instance of forum services.
   * @return
   */
  protected ForumsBM getForumsBM() {
    if (forumsBM == null) {
      try {
        forumsBM = EJBUtilitaire.getEJBObjectRef(JNDINames.FORUMSBM_EJBHOME, ForumsBM.class);
      } catch (Exception e) {
        throw new EJBException(e.getMessage(), e);
      }
    }
    return forumsBM;
  }
}