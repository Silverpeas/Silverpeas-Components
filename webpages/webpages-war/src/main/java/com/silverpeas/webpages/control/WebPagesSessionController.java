package com.silverpeas.webpages.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.silverpeas.webpages.model.WebPagesRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBm;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBmHome;

/**
 * @author sdevolder
 */
public class WebPagesSessionController extends AbstractComponentSessionController
{
    /**
     * Standard Session Controller Constructeur
     *
     *
     * @param mainSessionCtrl   The user's profile
     * @param componentContext  The component's profile
     *
     * @see
     */
	public WebPagesSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
		super(mainSessionCtrl, componentContext,  "com.silverpeas.webpages.multilang.webPagesBundle", "com.silverpeas.webpages.settings.webPagesIcons");
	}
	
	/**
	 * Méthode récupérant le role le plus élevé du user
	 * @return le role
	 */
	public String getProfile() {
		String[]profiles 	= getUserRoles();
		String 	flag 		= "user";
		String 	profile 	= "";
		for (int i=0; i < profiles.length; i++) {
			profile = profiles[i];
			// if admin, return it, we won't find a better profile
			if (profile.equals("admin")) return profile;
			if (profile.equals("publisher"))
				flag = profile;
			else if (profile.equals("writer")) {
				if (!flag.equals("publisher"))
					flag = profile;
			}
			else if (profile.equals("supervisor"))
			{
				flag = profile;
			}
		}
		return flag;
	}

	/**
	 * @return vrai s'il existe un fichier wysiwyg pour l'instance de composant
	 */
	public boolean haveGotWysiwygNotEmpty() {
		boolean returnValue = false;
		if (WysiwygController.haveGotWysiwyg(this.getSpaceId(),this.getComponentId(),this.getComponentId())) {
			try {
				String contenuWysiwyg = WysiwygController.loadFileAndAttachment(this.getSpaceId(),this.getComponentId(),this.getComponentId());
				if ((contenuWysiwyg != null) && (contenuWysiwyg.length() != 0)) {					
					returnValue = true;
				}
			} catch(WysiwygException ex) {
				SilverTrace.error("webPages","WebPagesSessionController.haveGotWysiwyg()","root.",ex);
			}
		}
		return returnValue;
	}
	
	public void index() {
		//try {
		//	WysiwygController.createIndex(this.getSpaceId(),this.getComponentId(),this.getComponentId(),this.getComponentLabel(),this.getComponentLabel(),null,null,this.getUserId());
		//}
		//catch(WysiwygException ex) {
		//}
	}

    /**************************************************************************************/
    /* webPages - Gestion des abonnements                                                   */
    /**************************************************************************************/
    public synchronized Collection getSubscriptionList() throws RemoteException
    {
	    return this.getSubscriptionList(getUserId(), getComponentId());
    }

    public synchronized void removeSubscription(String topicId) throws RemoteException
    {
    	this.removeSubscriptionToCurrentUser(getNodePK(topicId), getUserId());
    }

    public synchronized void addSubscription(String topicId) throws RemoteException {
    	
	    this.addSubscription(getNodePK(topicId), getUserId());
    }

    /**************************************************************************************/
    /* Interface - Gestion des abonnements                                                */
    /**************************************************************************************/
    /**
	* Subscriptions - get the subscription list of the current user
	* @return a Path Collection - it's a Collection of NodeDetail collection
	* @see com.stratelia.webactiv.util.node.model.NodeDetail
	* @since 1.0
	*/
    public Collection getSubscriptionList(String userId, String componentId) {
		  SilverTrace.info("webPages","WebPagesSessionController.getSubscriptionList()", "root.MSG_GEN_ENTER_METHOD");
          try {
			  Collection list = getSubscribeBm().getUserSubscribePKsByComponent(userId, componentId);
              Collection detailedList = new ArrayList();
              Iterator i = list.iterator();
    	      //For each favorite, get the path from root to favorite
              while (i.hasNext()) {
                   NodePK pk = (NodePK) i.next();
                   NodeDetail nodeDetail = new NodeDetail();
                   nodeDetail.setNodePK(pk);
                   //Collection path = getNodeBm().getPath(pk);
             	  detailedList.add(nodeDetail);
              }  
			  SilverTrace.info("webPages","WebPagesSessionController.getSubscriptionList()", "root.MSG_GEN_EXIT_METHOD");
              return detailedList;
          } catch (Exception e) {
				throw new WebPagesRuntimeException("WebPagesSessionController.getSubscriptionList()",SilverpeasRuntimeException.ERROR,"webPages.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
		  }
    }

  /**
	* Subscriptions - remove a subscription to the subscription list of the current user
	* @param topicId the subscribe topic Id to remove
	* @since 1.0
	*/
    public void removeSubscriptionToCurrentUser(NodePK topicPK, String userId) {
		  SilverTrace.info("webPages","WebPagesSessionController.removeSubscriptionToCurrentUser()", "root.MSG_GEN_ENTER_METHOD");
          try {
		  	getSubscribeBm().removeSubscribe(userId, topicPK);
          } catch (Exception e) {
				throw new WebPagesRuntimeException("WebPagesSessionController.removeSubscriptionToCurrentUser()",SilverpeasRuntimeException.ERROR,"webPages.EX_IMPOSSIBLE_DE_SUPPRIMER_ABONNEMENT", e);
		  }
		  SilverTrace.info("webPages","WebPagesSessionController.removeSubscriptionToCurrentUser()", "root.MSG_GEN_EXIT_METHOD");
    }

	private NodePK getNodePK(String id)
	{
		return new NodePK(id, getSpaceId(), getComponentId());
	}

	public NodeBm getNodeBm() {
	  NodeBm nodeBm = null;
	  try {
		  NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
	      nodeBm = nodeBmHome.create();
	  } catch (Exception e) {
		  throw new WebPagesRuntimeException("WebPagesSessionController.getNodeBm()",SilverpeasRuntimeException.ERROR, "webPages.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
	  }
	  return nodeBm;
	}

	/**
	* Subscriptions - add a subscription
	* @param topicId the subscription topic Id to add
	* @since 1.0
	*/
    public void addSubscription(NodePK topicPK, String userId) {
		SilverTrace.info("webPages","WebPagesSessionController.addSubscription()", "root.MSG_GEN_ENTER_METHOD");

        if (!checkSubscription(topicPK, userId))
        	return;

        try {
			getSubscribeBm().addSubscribe(userId, topicPK);
        } catch (Exception e) {
			SilverTrace.warn("webPages","WebPagesSessionController.addSubscription()", "webPages.EX_SUBSCRIPTION_ADD_FAILED", "topicId = "+topicPK.getId(), e);
        }
		SilverTrace.info("webPages", "WebPagesSessionController.addSubscription()", "root.MSG_GEN_EXIT_METHOD");
    }

    /**
     * @return true if this topic does not exists in user subscriptions and can be added to them.
     */
    public boolean checkSubscription(NodePK topicPK, String userId) {
        try {
			Collection subscriptions = getSubscribeBm().getUserSubscribePKsByComponent(userId, topicPK.getInstanceId());
            for (Iterator iterator = subscriptions.iterator(); iterator.hasNext();) {
                NodePK nodePK = (NodePK) iterator.next();
                if (topicPK.getId().equals(nodePK.getId())) {
                        return false;
                }
            }
            return true;

        } catch (Exception e) {
            throw new WebPagesRuntimeException("WebPagesSessionController.checkSubscription()",SilverpeasRuntimeException.ERROR,
                    "webPages.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
        }
    }

    public SubscribeBm getSubscribeBm() {
  	  SubscribeBm subscribeBm = null;
        try {
      	  SubscribeBmHome subscribeBmHome = (SubscribeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.SUBSCRIBEBM_EJBHOME, SubscribeBmHome.class);
      	  subscribeBm = subscribeBmHome.create();
        } catch (Exception e) {
      	  throw new WebPagesRuntimeException("WebPagesSessionController.getSubscribeBm()",SilverpeasRuntimeException.ERROR,"webPages.EX_IMPOSSIBLE_DE_FABRIQUER_SUBSCRIBEBM_HOME", e);
        }
        return subscribeBm;
    }
    
    /**
     * Return boolean if subscription is used for this instance
     * @return boolean
     */
	public boolean isSubscriptionUsed()
	{
		if ("yes".equalsIgnoreCase(getComponentParameterValue("useSubscription")))
			return true;
		return false;
	}

}
