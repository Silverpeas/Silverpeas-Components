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
package com.silverpeas.component.kmelia;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
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
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
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
		// TODO Auto-generated constructor stub
	}

	@Override
	public void paste(PasteDetail pasteDetail) throws RemoteException {
        SilverTrace.debug("kmelia","KmeliaPaste.paste()","root.MSG_GEN_ENTER_METHOD");
		// TODO Auto-generated method stub
		fromComponentId = pasteDetail.getFromComponentId();
		toComponentId = pasteDetail.getToComponentId();
		userId = pasteDetail.getUserId();

		//Get root node Detail
		NodeDetail father = getNodeBm().getDetail(getNodePK("0", toComponentId));

		//Get level 1 nodes
		NodePK rootPK = getNodePK("0", fromComponentId);
		List<NodeDetail> firstLevelNodes = getNodeBm().getHeadersByLevel(rootPK, 2);
		for (int i=0; i<firstLevelNodes.size(); i++)
		{
			NodeDetail nodeToPaste = (NodeDetail) firstLevelNodes.get(i);
			if (nodeToPaste.getId() > 2)	//Don't take unbalanced and basket nodes
				pasteNode(nodeToPaste, father, false);
		}
        SilverTrace.debug("kmelia","KmeliaPaste.paste()","root.MSG_GEN_EXIT_METHOD");
	}
	
	/**
	 * Paste Topic
	 * @param nodeToPaste
	 * @param father
	 * @param isCutted
	 * @throws RemoteException
	 */
	private void pasteNode(NodeDetail nodeToPaste, NodeDetail father, boolean isCutted) throws RemoteException
	{
        SilverTrace.debug("kmelia","KmeliaPaste.pasteNode()","root.MSG_GEN_ENTER_METHOD");
		NodePK nodeToPastePK = nodeToPaste.getNodePK();
		List treeToPaste = getNodeBm().getSubTree(nodeToPastePK);
		
		if (isCutted)
		{
			//move node and subtree
			getNodeBm().moveNode(nodeToPastePK, father.getNodePK());
			
			NodeDetail fromNode = null;
			NodePK toNodePK = null;
			for (int i=0; i<treeToPaste.size(); i++)
			{
				fromNode = (NodeDetail) treeToPaste.get(i);
				if (fromNode != null)
				{
					toNodePK = getNodePK(fromNode.getNodePK().getId());
					
					//move wysiwyg
					try {
						AttachmentController.moveAttachments(new ForeignPK("Node_"+fromNode.getNodePK()), new ForeignPK("Node_"+toNodePK.getId(), toComponentId), true);  //Change instanceId + move files
					} catch (AttachmentException e) {
						SilverTrace.error("kmelia", "KmeliaPaste.pasteNode()", "root.MSG_GEN_PARAM_VALUE","kmelia.CANT_MOVE_ATTACHMENTS", e);
					}
					
					//change images path in wysiwyg
					try {
						WysiwygController.wysiwygPlaceHaveChanged(fromNode.getNodePK().getInstanceId(), "Node_"+fromNode.getNodePK().getId(), toComponentId, "Node_"+toNodePK.getId());
					} catch (WysiwygException e) {
						SilverTrace.error("kmelia", "KmeliaPaste.pasteNode()", "root.MSG_GEN_PARAM_VALUE", e);
					}
				}
			}
		}
		else
		{
			//paste topic
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
			if (nodeToPaste.haveLocalRights())
				node.setRightsDependsOn(new Integer(nodePK.getId()).intValue());
			else
				node.setRightsDependsOn(-1);
			getNodeBm().updateRightsDependency(node);
			
			//Set topic rights if necessary
			if (nodeToPaste.haveLocalRights())
			{
				List<ProfileInst> topicProfiles = getTopicProfiles(nodeToPaste.getNodePK().getId());
				for (int i=0; i<topicProfiles.size(); i++)
				{
					ProfileInst nodeToPasteProfile = (ProfileInst) topicProfiles.get(i);
					if (nodeToPasteProfile != null)
					{
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
			
			//paste wysiwyg attached to node
			WysiwygController.copy(null, nodeToPastePK.getInstanceId(), "Node_"+nodeToPastePK.getId(), null, toComponentId, "Node_"+nodePK.getId(), userId);
			
			List nodeIdsToPaste = new ArrayList();
			NodeDetail oneNodeToPaste = null;
			for (int i=0; i<treeToPaste.size(); i++)
			{
				oneNodeToPaste = (NodeDetail) treeToPaste.get(i);
				if (oneNodeToPaste != null)
					nodeIdsToPaste.add(oneNodeToPaste.getNodePK());
			}
			
			//paste subtopics
			node = getNodeBm().getHeader(nodePK);
			Collection subtopics = getNodeBm().getDetail(nodeToPastePK).getChildrenDetails();
			Iterator itSubTopics = subtopics.iterator();
			NodeDetail subTopic = null;
			while (itSubTopics != null && itSubTopics.hasNext())
			{
				subTopic = (NodeDetail) itSubTopics.next();
				if (subTopic != null)
					pasteNode(subTopic, node, isCutted);
			}
		}
        SilverTrace.debug("kmelia","KmeliaPaste.pasteNode()","root.MSG_GEN_EXIT_METHOD");
	}

	public List<ProfileInst> getTopicProfiles(String topicId)
    {
        SilverTrace.debug("kmelia","KmeliaPaste.getTopicProfiles()","root.MSG_GEN_ENTER_METHOD");
    	List alShowProfile = new ArrayList();
    	ProfileInst profile = null;

    	//profils dispo
    	String[] asAvailProfileNames = getAdmin().getAllProfilesNames("kmelia");

        for(int nI=0;  nI < asAvailProfileNames.length; nI++)
        {
        	SilverTrace.info("kmelia","KmeliaPaste.getTopicProfiles()","root.MSG_GEN_PARAM_VALUE","asAvailProfileNames = "+asAvailProfileNames[nI]);
        	profile = getTopicProfile(asAvailProfileNames[nI], topicId);
        	profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileNames[nI]));
        	alShowProfile.add(profile);
        }
        SilverTrace.debug("kmelia","KmeliaPaste.getTopicProfiles()","root.MSG_GEN_EXIT_METHOD");
        return alShowProfile;
    }

	public ProfileInst getTopicProfile(String role, String topicId)
	{
        SilverTrace.debug("kmelia","KmeliaPaste.getTopicProfile()","root.MSG_GEN_ENTER_METHOD");
		List<ProfileInst> profiles = getAdmin().getProfilesByObject(topicId, ObjectType.NODE, fromComponentId);
		for (int p=0; profiles != null && p<profiles.size(); p++)
		{
			ProfileInst profile = (ProfileInst) profiles.get(p);
			if (profile.getName().equals(role))
				return profile;
		}

		ProfileInst profile = new ProfileInst();
		profile.setName(role);
        SilverTrace.debug("kmelia","KmeliaPaste.getTopicProfile()","root.MSG_GEN_EXIT_METHOD");
		return profile;
	}
	
	private NodeBm getNodeBm() {
		  NodeBm nodeBm = null;
		  try {
			  NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
	          nodeBm = nodeBmHome.create();
		  } catch (Exception e) {
			  throw new KmeliaRuntimeException("PasteDetail.getNodeBm()",SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
		  }
		  return nodeBm;
	}
	
    public synchronized List<NodeDetail> getAllTopics() throws RemoteException
    {
   		return getNodeBm().getSubTree(getNodePK("0"));
    }
	
	private NodePK getNodePK(String id, String componentId)
	{
		return new NodePK(id, componentId);
	}

	private NodePK getNodePK(String id)
	{
		return new NodePK(id, fromComponentId);
	}

	private AdminController getAdmin()
	{
		if (m_AdminCtrl == null)
			m_AdminCtrl = new AdminController(userId);
		
		return m_AdminCtrl;
	}

}
