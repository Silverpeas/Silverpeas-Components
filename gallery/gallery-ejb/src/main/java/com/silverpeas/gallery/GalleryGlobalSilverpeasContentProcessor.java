/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery;

import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.processing.Size;
import com.stratelia.silverpeas.contentManager.DefaultGlobalSilverContentProcessor;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.contentManager.IGlobalSilverContentProcessor;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;

import javax.inject.Named;
import java.io.IOException;

@Named("galleryGlobalSilverpeasContentProcessor")
public class GalleryGlobalSilverpeasContentProcessor extends DefaultGlobalSilverContentProcessor
    implements IGlobalSilverContentProcessor {

  @Override
  public GlobalSilverContent getGlobalSilverContent(SilverContentInterface sci,
      UserDetail creatorDetail, String location) {
    GlobalSilverContent gsc = super.getGlobalSilverContent(sci, creatorDetail, location);
    String instanceId = sci.getInstanceId();
    Media media = (Media) sci;
    gsc.setThumbnailURL(media.getApplicationThumbnailUrl(MediaResolution.TINY));
    InternalMedia internalMedia = media.getInternalMedia();
    if (internalMedia != null) {
      Size size = new Size(MediaResolution.TINY.getWidth(), MediaResolution.TINY.getHeight());
      try {
        size = MediaUtil.getWidthAndHeight(instanceId, media.getWorkspaceSubFolderName(),
            internalMedia.getFileName(), MediaResolution.TINY.getWidth());
      } catch (IOException e) {
        SilverTrace
            .info("gallery", "GalleryGlobalSilverpeasContentProcessor.getGlobalSilverContent",
                "root.MSG_GEN_PARAM_VALUE", "Error during processing size !", e);
      }
      gsc.setThumbnailWidth(String.valueOf(size.getWidth()));
      gsc.setThumbnailHeight(String.valueOf(size.getHeight()));
    }
    return gsc;
  }

}
