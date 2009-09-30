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