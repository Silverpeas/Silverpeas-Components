/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.yellowpages;

import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexation;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.contact.model.ContactDetail;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.yellowpages.control.ejb.YellowpagesBm;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;
import com.stratelia.webactiv.yellowpages.model.UserContact;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class YellowpagesIndexer implements ComponentIndexation {

  private static String ROOT_TOPIC_ID = "0";

  @Inject
  private YellowpagesBm yellowpagesBm;
  @Inject
  private NodeService nodeService;

  @Override
  public void index(ComponentInst componentInst) throws Exception {
    indexTopic(componentInst.getId(), ROOT_TOPIC_ID);
  }

  // TODO the code below is incorrect. Nicolas is in fixing it. So, change it manually once done.
  private void indexTopic(String componentId, String topicId) throws Exception {
    /*
    if (!topicId.startsWith(YellowpagesSessionController.GroupReferentielPrefix)) {
      TopicDetail topic = getTopic(componentId, topicId);
      if (!topicId.equals(ROOT_TOPIC_ID) && !topicId.equals("1") && !topicId.equals("2")) {
        NodeDetail nodeDetail = topic.getNodeDetail();
        nodeDetail.getFatherPK().setComponentName(componentId);
        nodeDetail.getNodePK().setComponentName(componentId);
        yellowpagesBm.updateTopic(nodeDetail);
        //scc.updateTopicHeader(topic.getNodeDetail());
      }

      // treatment of the publications of current topic
      indexTopicContacts(componentId, topic);

      // treatment of the nodes of current topic
      Collection<NodeDetail> subTopics = topic.getNodeDetail().getChildrenDetails();
      for (NodeDetail node : subTopics) {
        indexTopic(componentId, node.getNodePK().getId());
      }
    }
    */
  }

  private void indexTopicContacts(String componentId, TopicDetail topic) throws Exception {
    Collection<UserContact> contacts = topic.getContactDetails();
    for (UserContact contact : contacts) {
      ContactDetail contactDetail = contact.getContact();
      contactDetail.getPK().setComponentName(componentId);
      contactDetail.setCreationDate(new Date());
      contactDetail.setCreatorId(UserDetail.getCurrentRequester().getId());
      yellowpagesBm.updateContact(contactDetail);
    }
  }

  private TopicDetail getTopic(String componentId, String id) {
    NodePK pk = new NodePK(id, componentId);
    TopicDetail topic = yellowpagesBm.goTo(pk, UserDetail.getCurrentRequester().getId());
    List<NodeDetail> thePath = (List<NodeDetail>) nodeService.getAnotherPath(pk);
    Collections.reverse(thePath);
    topic.setPath(thePath);
    return topic;
  }
}
