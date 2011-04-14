/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.gallery;

import java.io.IOException;

import com.silverpeas.gallery.model.PhotoDetail;
import com.stratelia.silverpeas.contentManager.DefaultGlobalSilverContentProcessor;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.contentManager.IGlobalSilverContentProcessor;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;

public class GalleryGlobalSilverpeasContentProcessor extends DefaultGlobalSilverContentProcessor
    implements IGlobalSilverContentProcessor {

  private static String galleryDirectory = null;
  static {
    ResourceLocator gallerySettings =
        new ResourceLocator("com.silverpeas.gallery.settings.gallerySettings", "");
    galleryDirectory = gallerySettings.getString("imagesSubDirectory");
  }

  @Override
  public GlobalSilverContent getGlobalSilverContent(SilverContentInterface sci,
      UserDetail creatorDetail, String location) {
    GlobalSilverContent gsc = super.getGlobalSilverContent(sci, creatorDetail, location);

    String instanceId = sci.getInstanceId();

    String directory = galleryDirectory + sci.getId();
    PhotoDetail photo = (PhotoDetail) sci;
    gsc.setThumbnailURL(FileServerUtils.getUrl(null, instanceId, photo.getImageName(), photo
        .getImageMimeType(), directory));
    String[] widthAndHeight = { "60", "45" };
    try {
      widthAndHeight =
          ImageHelper.getWidthAndHeight(instanceId, directory, photo.getImageName(), 60);
    } catch (IOException e) {
      SilverTrace.info("gallery",
          "GalleryGlobalSilverpeasContentProcessor.getGlobalSilverContent",
          "root.MSG_GEN_PARAM_VALUE", "Error during processing size !");
    }
    gsc.setThumbnailWidth(widthAndHeight[0]);
    gsc.setThumbnailHeight(widthAndHeight[1]);

    return gsc;
  }

}
