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
package org.silverpeas.components.gallery.servlets;

import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.core.web.util.servlet.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.util.ServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public class GoToImage extends GoTo {

  private static final long serialVersionUID = -8077728285107343008L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    MediaPK mediaPK = new MediaPK(objectId);
    Media media = getGalleryBm().getMedia(mediaPK);
    String componentId = media.getMediaPK().getInstanceId();

    String gotoURL = URLManager.getURL(null, componentId) + media.getURL();

    return "goto=" + URLEncoder.encode(gotoURL, CharEncoding.UTF_8);
  }

  private GalleryService getGalleryBm() {
    GalleryService currentGalleryService = null;
    try {
      currentGalleryService = ServiceProvider.getService(GalleryService.class);
    } catch (Exception e) {
      displayError(null);
    }
    return currentGalleryService;
  }
}