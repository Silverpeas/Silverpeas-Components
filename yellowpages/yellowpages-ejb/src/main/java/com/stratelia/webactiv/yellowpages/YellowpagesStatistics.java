/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.yellowpages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.yellowpages.control.ejb.YellowpagesBm;
import com.stratelia.webactiv.yellowpages.control.ejb.YellowpagesBmHome;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;
import com.stratelia.webactiv.yellowpages.model.UserContact;


/**
 * Class declaration
 *
 *
 * @author
 */
public class YellowpagesStatistics implements ComponentStatisticsInterface
{
    private YellowpagesBm kscEjb = null;

    /**
     * Method declaration
     *
     *
     * @param spaceId
     * @param componentId
     *
     * @return
     *
     * @throws Exception
     *
     * @see
     */
    public Collection getVolume(String spaceId, String componentId) throws Exception
    {
        ArrayList  myArrayList = new ArrayList();

        Collection c = null;

        c = getContacts("0", spaceId, componentId);

        if (c == null)
        {
            return null;
        }
        else
        {
            Iterator iter = c.iterator();

            while (iter.hasNext())
            {
                ContactDetail           detail = ((UserContact) iter.next()).getContact();

                UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

                myCouple.setUserId(detail.getCreatorId());
                myCouple.setCountVolume(1);
                myArrayList.add(myCouple);
            }
        }

        return myArrayList;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    private YellowpagesBm getYellowpagesBm()
    {
        if (kscEjb == null)
        {
            try
            {
                YellowpagesBmHome kscEjbHome = (YellowpagesBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.YELLOWPAGESBM_EJBHOME, YellowpagesBmHome.class);

                kscEjb = kscEjbHome.create();
            }
            catch (Exception e)
            {
                throw new EJBException(e);
            }
        }
        return kscEjb;
    }

    /**
     * Method declaration
     *
     *
     * @param topicId
     * @param spaceId
     * @param componentId
     *
     * @return
     *
     * @throws Exception
     *
     * @see
     */
    private Collection getContacts(String topicId, String spaceId, String componentId) throws Exception
    {
        Collection             c = new ArrayList();
        if (topicId == null || topicId.startsWith("group_"))
        	return c;
        
        OrganizationController myOrga = new OrganizationController();
        
        if (topicId.startsWith("group_"))
        {
        	int nbUsers = myOrga.getAllSubUsersNumber(topicId.substring("group_".length()));
        	for (int n=0; n<nbUsers; n++)
        	{
        		ContactDetail detail = new ContactDetail("useless", "useless", "useless", "useless", "useless", "useless", "useless", new Date(), "0");
        		c.add(detail);
        	}
        }
        else
        {
	        getYellowpagesBm().setActor(myOrga.getUserDetail("0"));
	        getYellowpagesBm().setPrefixTableName(spaceId);
	        getYellowpagesBm().setComponentId(componentId);
	
	        TopicDetail topic = null;
	
	        try
	        {
	            topic = getYellowpagesBm().goTo(topicId);
	            if (topic != null)
	            {
	                c.addAll(topic.getContactDetails());
	            }
	        }
	        catch (Exception ex)
	        {
	            topic = null;
	            SilverTrace.error("silverstatistics", "YellowpagesStatistics.getContacts()", "root.MSG_GEN_PARAM_VALUE", ex);
	        }
	        // treatment of the nodes of current topic
	        if (topic != null)
	        {
	            Collection subTopics = topic.getNodeDetail().getChildrenDetails();
	
	            if (subTopics != null)
	            {
	                Iterator itNode = subTopics.iterator();
	
	                while (itNode.hasNext())
	                {
	                    NodeDetail node = (NodeDetail) itNode.next();
	
	                    if (!(node.getNodePK().getId().equals("0") || node.getNodePK().getId().equals("1") || node.getNodePK().getId().equals("2")))
	                    {
	                        c.addAll(getContacts(node.getNodePK().getId(), spaceId, componentId));
	                    }
	                }
	            }
	        }
        }
        return c;
    }

}
