package com.silverpeas.gallery.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

public class GoToImage extends GoTo {
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    PhotoPK photoPK = new PhotoPK(objectId);
    PhotoDetail photo = getGalleryBm().getPhoto(photoPK);
    String componentId = photo.getPhotoPK().getInstanceId();

    SilverTrace.info("gallery", "GoToImage.doPost", "root.MSG_GEN_PARAM_VALUE",
        "componentId = " + componentId);

    String gotoURL = URLManager.getURL(null, componentId) + photo.getURL();

    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

  private GalleryBm getGalleryBm() {
    GalleryBm currentGalleryBm = null;
    try {
      GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      currentGalleryBm = galleryBmHome.create();
    } catch (Exception e) {
      displayError(null);
    }
    return currentGalleryBm;
  }
}