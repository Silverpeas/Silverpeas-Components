/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.process.media;

import com.silverpeas.gallery.delegate.MediaDataUpdateDelegate;
import com.silverpeas.gallery.model.Media;
import org.silverpeas.process.session.ProcessSession;

import com.silverpeas.form.PagesContext;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Process to update a media in Database
 * @author Yohann Chastagnier
 */
public class GalleryUpdateMediaDataProcess extends AbstractGalleryDataProcess {

  /**
   * Delegate in charge of updating media data
   */
  private final MediaDataUpdateDelegate delegate;
  private final boolean updateTechnicalData;

  /**
   * Gets an instance
   * @param media
   * @return
   */
  public static GalleryUpdateMediaDataProcess getInstance(final Media media) {
    return new GalleryUpdateMediaDataProcess(media, null, false);
  }

  /**
   * Gets an instance
   * @param media
   * @param delegate
   * @return
   */
  public static GalleryUpdateMediaDataProcess getInstance(final Media media,
      final MediaDataUpdateDelegate delegate) {
    return new GalleryUpdateMediaDataProcess(media, delegate, true);
  }

  /**
   * Default hidden constructor
   * @param media
   * @param delegate
   * @param updateTechnicalData
   */
  protected GalleryUpdateMediaDataProcess(final Media media, final MediaDataUpdateDelegate delegate,
      final boolean updateTechnicalData) {
    super(media);
    this.delegate = delegate;
    this.updateTechnicalData = updateTechnicalData;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.gallery.process.AbstractGalleryDataProcess#processData(com.silverpeas.gallery
   * .process.GalleryProcessExecutionContext, org.silverpeas.process.session.ProcessSession)
   */
  @Override
  protected void processData(final GalleryProcessExecutionContext context,
      final ProcessSession session) throws Exception {

    // Sets functional data media
    if (delegate != null) {

      SilverTrace
          .info("gallery", "GalleryUpdateMediaDataProcess.process()", "root.MSG_GEN_ENTER_METHOD",
              "MediaPK = " + getMedia().toString());

      if (delegate.isHeaderData()) {
        delegate.updateHeader(getMedia());
      }

      // Persists form data
      if (delegate.isForm()) {
        final String mediaId = getMedia().getId();
        final PagesContext pageContext =
            new PagesContext("mediaForm", "0", delegate.getLanguage(), false,
                context.getComponentInstanceId(), context.getUser().getId(), delegate.getAlbumId());
        pageContext.setEncoding("UTF-8");
        pageContext.setObjectId(mediaId);
        if (delegate.isSkipEmptyValues()) {
          pageContext.setUpdatePolicy(PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES);
        }
        delegate.updateForm(mediaId, pageContext);
      }
    }

    // Update media
    updateMedia(updateTechnicalData, context);
  }
}
