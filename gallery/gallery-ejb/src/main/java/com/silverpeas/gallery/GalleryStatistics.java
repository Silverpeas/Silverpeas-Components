package com.silverpeas.gallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class GalleryStatistics implements ComponentStatisticsInterface
{

	public Collection getVolume(String spaceId, String componentId) throws Exception
	{
		ArrayList  myArrayList = new ArrayList();
				
		Collection photos = getGalleryBm().getAllPhotos(componentId);
		Iterator   iter = photos.iterator();
        while (iter.hasNext())
        {
            PhotoDetail photo = (PhotoDetail) iter.next();
            UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
            myCouple.setUserId(photo.getCreatorId());
            myCouple.setCountVolume(1);
            myArrayList.add(myCouple);
        }

        return myArrayList;
	}
	
	private GalleryBm getGalleryBm()
	{
		GalleryBm galleryBm = null;
		try
		{
			GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire
					.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME,
							GalleryBmHome.class);
			galleryBm = galleryBmHome.create();
		}
		catch (Exception e)
		{
			throw new GalleryRuntimeException("GallerySessionController.getGalleryBm()",
					SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
					e);
		}
		return galleryBm;
	}
    
}
