/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.newsEdito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 *
 *
 * @author
 */
public class NewsEditoStatistics implements ComponentStatisticsInterface
{

    private NodeBm  nodeBm=null;

    public Collection getVolume(String spaceId, String componentId) throws Exception
    {
        ArrayList  myArrayList = new ArrayList();
        Collection c = getElements(spaceId, componentId);
        Iterator   iter = c.iterator();

        while (iter.hasNext())
        {
            NodeDetail detail = (NodeDetail) iter.next();

            UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

            myCouple.setUserId(detail.getCreatorId());
            myCouple.setCountVolume(1);
            myArrayList.add(myCouple);
        }

        return myArrayList;
    }


    private NodeBm getNodeBm()
    {
        if (nodeBm == null)
        {
            try
            {
                nodeBm = ((NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).create();
            }
            catch (Exception e)
            {
                throw new EJBException(e);
            }
        }
        return nodeBm;
    }

    private Collection getElements(String spaceId, String componentId) throws Exception
    {
        // recuperation des journaux
        Collection archives = getNodeBm().getFrequentlyAskedChildrenDetails(new NodePK("0", spaceId, componentId));
        return archives;
    }
}
