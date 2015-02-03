/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.forums;

import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexation;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.models.MessagePK;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;

import static com.stratelia.webactiv.forums.forumsManager.ejb.ForumsServiceProvider
    .getForumsService;

@Singleton
@Named("ForumsComponentIndexation")
public class ForumsIndexer implements ComponentIndexation {

  private static String ROOT_FORUM_ID = "0";

  @Override
  public void index(ComponentInst componentInst) throws Exception {
    indexForum(componentInst.getId(), ROOT_FORUM_ID);
  }

  private void indexForum(String componentId, String forumId) throws Exception {
    ForumPK pk = new ForumPK(componentId, forumId);
    List<String> sonIds = getForumsService().getForumSonsIds(pk);
    for (String aSonId : sonIds) {
      indexForum(componentId, aSonId);
    }
    if (!ROOT_FORUM_ID.equals(forumId)) {
      getForumsService().createIndex(pk);
    }

    Collection<Message> messages = getForumsService().getMessages(pk);
    for (Message aMessage : messages) {
      indexMessageNoRecursive(componentId, aMessage.getId());
    }
  }

  private void indexMessageNoRecursive(String componentId, int messageId) throws Exception {
    getForumsService().createIndex(new MessagePK(componentId, String.valueOf(messageId)));
  }

}