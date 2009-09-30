/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.stratelia.webactiv.yellowpages;

import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.yellowpages.control.YellowpagesSessionController;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;
import com.stratelia.webactiv.yellowpages.model.UserContact;

public class YellowpagesIndexer implements ComponentIndexerInterface {

  private YellowpagesSessionController scc = null;

  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {

    scc = new YellowpagesSessionController(mainSessionCtrl, context);
    indexTopic("0");
  }

  private void indexTopic(String topicId) throws Exception {
    if (!topicId
        .startsWith(YellowpagesSessionController.GroupReferentielPrefix)) {
      TopicDetail topic = scc.getTopic(topicId);

      if (!topicId.equals("0") && !topicId.equals("1") && !topicId.equals("2"))
        scc.updateTopicHeader(topic.getNodeDetail());

      // treatment of the publications of current topic
      indexTopicContacts(topic);

      // treatment of the nodes of current topic
      Collection subTopics = topic.getNodeDetail().getChildrenDetails();
      Iterator itNode = subTopics.iterator();
      String nodeId = null;
      NodeDetail node = null;
      while (itNode.hasNext()) {
        node = (NodeDetail) itNode.next();
        nodeId = node.getNodePK().getId();
        indexTopic(nodeId);
      }
    }
  }

  private void indexTopicContacts(TopicDetail topic) throws Exception {
    Collection contacts = topic.getContactDetails();
    Iterator itPub = contacts.iterator();
    while (itPub.hasNext()) {
      ContactDetail pub = ((UserContact) itPub.next()).getContact();
      scc.updateContact(pub);
    }
  }

}