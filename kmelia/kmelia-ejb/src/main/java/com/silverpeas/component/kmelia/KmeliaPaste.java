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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.component.kmelia;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentPasteInterface;
import com.stratelia.webactiv.beans.admin.instance.control.PasteDetail;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

public class KmeliaPaste implements ComponentPasteInterface {

  private AdminController m_AdminCtrl = null;

  String fromComponentId;
  String toComponentId;
  String userId;

  public KmeliaPaste() {
  }

  @Override
  public void paste(PasteDetail pasteDetail) throws RemoteException {
    SilverTrace.debug("kmelia", "KmeliaPaste.paste()", "root.MSG_GEN_ENTER_METHOD");
    fromComponentId = pasteDetail.getFromComponentId();
    toComponentId = pasteDetail.getToComponentId();
    userId = pasteDetail.getUserId();

    // Get root node Detail
    NodeDetail father = getNodeBm().getDetail(getNodePK("0", toComponentId));

    // Get level 1 nodes
    NodePK rootPK = getNodePK("0", fromComponentId);
    List<NodeDetail> firstLevelNodes = getNodeBm().getHeadersByLevel(rootPK, 2);
    HashMap<Integer, Integer> oldAndNewIds = new HashMap<Integer, Integer>();
    for (int i = 0; i < firstLevelNodes.size(); i++) {
      NodeDetail nodeToPaste = (NodeDetail) firstLevelNodes.get(i);
      if (nodeToPaste.getId() > 2) // Don't take unbalanced and basket nodes
        pasteNode(nodeToPaste, father, oldAndNewIds);
    }
    SilverTrace.debug("kmelia", "KmeliaPaste.paste()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Paste Topic
   * @param nodeToPaste
   * @param father
   * @param isCutted
   * @throws RemoteException
   */
  private void pasteNode(NodeDetail nodeToPaste, NodeDetail father,
      HashMap<Integer, Integer> oldAndNewIds) throws RemoteException {
    SilverTrace.debug("kmelia", "KmeliaPaste.pasteNode()", "root.MSG_GEN_ENTER_METHOD");
    NodePK nodeToPastePK = nodeToPaste.getNodePK();

    // paste topic
    NodePK nodePK = new NodePK("unknown", toComponentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodePK);
    node.setCreatorId(userId);
    node.setName(nodeToPaste.getName());
    node.setDescription(nodeToPaste.getDescription());
    node.setTranslations(nodeToPaste.getTranslations());
    node.setCreationDate(DateUtil.today2SQLDate());
    node.setStatus(nodeToPaste.getStatus());
    nodePK = getNodeBm().createNode(node, father);
    oldAndNewIds.put(Integer.parseInt(nodeToPastePK.getId()), Integer.parseInt(nodePK.getId()));
    if (nodeToPaste.haveRights()) {
      if (nodeToPaste.haveLocalRights()) {
        node.setRightsDependsOn(Integer.parseInt(nodePK.getId()));
      } else {
        int oldRightsDependsOn = nodeToPaste.getRightsDependsOn();
        Integer newRightsDependsOn = oldAndNewIds.get(Integer.valueOf(oldRightsDependsOn));
        node.setRightsDependsOn(newRightsDependsOn.intValue());
      }
      getNodeBm().updateRightsDependency(node);
    }
    // Set topic rights if necessary
    if (nodeToPaste.haveLocalRights()) {
      List<ProfileInst> topicProfiles = getTopicProfiles(nodeToPaste.getNodePK().getId());
      for (int i = 0; i < topicProfiles.size(); i++) {
        ProfileInst nodeToPasteProfile = (ProfileInst) topicProfiles.get(i);
        if (nodeToPasteProfile != null) {
          ProfileInst nodeProfileInst = (ProfileInst) nodeToPasteProfile.clone();
          nodeProfileInst.setId("-1");
          nodeProfileInst.setComponentFatherId(toComponentId);
          nodeProfileInst.setObjectId(new Integer(nodePK.getId()).intValue());
          nodeProfileInst.setObjectType(nodeToPasteProfile.getObjectType());
          nodeProfileInst.setObjectFatherId(father.getId());
          // Add the profile
          m_AdminCtrl.addProfileInst(nodeProfileInst, userId);
        }
      }
    }

    // paste wysiwyg attached to node
    WysiwygController.copy(null, nodeToPastePK.getInstanceId(), "Node_" + nodeToPastePK.getId(),
        null, toComponentId, "Node_" + nodePK.getId(), userId);

    // paste subtopics
    node = getNodeBm().getHeader(nodePK);
    Collection<NodeDetail> subtopics = getNodeBm().getDetail(nodeToPastePK).getChildrenDetails();
    Iterator<NodeDetail> itSubTopics = subtopics.iterator();
    NodeDetail subTopic = null;
    while (itSubTopics != null && itSubTopics.hasNext()) {
      subTopic = itSubTopics.next();
      if (subTopic != null)
        pasteNode(subTopic, node, oldAndNewIds);
    }

    SilverTrace.debug("kmelia", "KmeliaPaste.pasteNode()", "root.MSG_GEN_EXIT_METHOD");
  }

  public List<ProfileInst> getTopicProfiles(String topicId) {
    SilverTrace.debug("kmelia", "KmeliaPaste.getTopicProfiles()", "root.MSG_GEN_ENTER_METHOD");
    List<ProfileInst> alShowProfile = new ArrayList<ProfileInst>();
    ProfileInst profile = null;

    // profils dispo
    String[] asAvailProfileNames = getAdmin().getAllProfilesNames("kmelia");

    for (int nI = 0; nI < asAvailProfileNames.length; nI++) {
      SilverTrace.info("kmelia", "KmeliaPaste.getTopicProfiles()", "root.MSG_GEN_PARAM_VALUE",
          "asAvailProfileNames = " + asAvailProfileNames[nI]);
      profile = getTopicProfile(asAvailProfileNames[nI], topicId);
      profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileNames[nI]));
      alShowProfile.add(profile);
    }
    SilverTrace.debug("kmelia", "KmeliaPaste.getTopicProfiles()", "root.MSG_GEN_EXIT_METHOD");
    return alShowProfile;
  }

  public ProfileInst getTopicProfile(String role, String topicId) {
    SilverTrace.debug("kmelia", "KmeliaPaste.getTopicProfile()", "root.MSG_GEN_ENTER_METHOD");
    List<ProfileInst> profiles =
        getAdmin().getProfilesByObject(topicId, ObjectType.NODE, fromComponentId);
    for (int p = 0; profiles != null && p < profiles.size(); p++) {
      ProfileInst profile = (ProfileInst) profiles.get(p);
      if (profile.getName().equals(role))
        return profile;
    }

    ProfileInst profile = new ProfileInst();
    profile.setName(role);
    SilverTrace.debug("kmelia", "KmeliaPaste.getTopicProfile()", "root.MSG_GEN_EXIT_METHOD");
    return profile;
  }

  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome =
          (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("PasteDetail.getNodeBm()", SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return nodeBm;
  }

  public synchronized List<NodeDetail> getAllTopics() throws RemoteException {
    return getNodeBm().getSubTree(getNodePK("0"));
  }

  private NodePK getNodePK(String id, String componentId) {
    return new NodePK(id, componentId);
  }

  private NodePK getNodePK(String id) {
    return new NodePK(id, fromComponentId);
  }

  private AdminController getAdmin() {
    if (m_AdminCtrl == null)
      m_AdminCtrl = new AdminController(userId);

    return m_AdminCtrl;
  }

}
