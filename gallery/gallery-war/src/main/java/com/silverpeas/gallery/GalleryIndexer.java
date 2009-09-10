package com.silverpeas.gallery;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class GalleryIndexer implements ComponentIndexerInterface 
{

    public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws Exception 
    {
    	getGalleryBm().indexGallery(context.getCurrentComponentId());
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
