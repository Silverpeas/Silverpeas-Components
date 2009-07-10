package com.silverpeas.resourcesmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;

import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBm;
import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBmHome;
import com.silverpeas.resourcesmanager.model.ReservationDetail;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;

public class ResourcesManagerStatistics implements ComponentStatisticsInterface 
{
	@Override
	public Collection getVolume(String spaceId, String componentId) throws Exception 
	{
		List<UserIdCountVolumeCouple> volumes = new ArrayList<UserIdCountVolumeCouple>();
		
		List<ReservationDetail> allReservations = getResourcesManagerBm().getReservations(componentId);
		for (Iterator iterator = allReservations.iterator(); iterator.hasNext();) {
			ReservationDetail reservationDetail = (ReservationDetail) iterator.next();
			
			UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

            myCouple.setUserId(reservationDetail.getUserId());
            myCouple.setCountVolume(1);
            volumes.add(myCouple);
		}
		
		return volumes;
		
	}
	
	private ResourcesManagerBm getResourcesManagerBm()
	{
		try
        {
            ResourcesManagerBm bm = ((ResourcesManagerBmHome) EJBUtilitaire.getEJBObjectRef("ejb/ResourcesManagerBm", ResourcesManagerBmHome.class)).create();
            return bm;
        }
        catch (Exception e)
        {
            SilverTrace.error("resourcesManager", "ResourcesManagerStatistics.getResourcesManagerBm", "root.MSG_EJB_CREATE_FAILED", e);
            throw new EJBException(e);
        }
	}
}
