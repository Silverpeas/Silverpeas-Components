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

import com.silverpeas.form.PagesContext;
import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.delegate.MediaDataCreateDelegate;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import org.silverpeas.util.CollectionUtil;
import org.silverpeas.process.session.ProcessSession;

import java.util.List;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

/**
 * Process to create a media in Database
 * @author Yohann Chastagnier
 */
public class GalleryCreateMediaDataProcess extends AbstractGalleryDataProcess {

  private final String albumId;

  /**
   * Delegate in charge of creating media data
   */
  private final MediaDataCreateDelegate delegate;

  /**
   * Gets an instance
   * @param media
   * @param delegate
   * @return
   */
  public static GalleryCreateMediaDataProcess getInstance(final Media media, final String albumId,
      final MediaDataCreateDelegate delegate) {
    return new GalleryCreateMediaDataProcess(media, albumId, delegate);
  }

  /**
   * Default hidden constructor
   * @param media
   * @param delegate
   */
  protected GalleryCreateMediaDataProcess(final Media media, final String albumId,
      final MediaDataCreateDelegate delegate) {
    super(media);
    this.delegate = delegate;
    this.albumId = albumId;
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

    // Media
    if (delegate.isHeaderData()) {
      delegate.updateHeader(getMedia());
    }

    createMedia(albumId, context);

    // Persists form data
    if (delegate.isForm()) {
      final String mediaId = getMedia().getId();
      final PagesContext pageContext =
          new PagesContext("mediaForm", "0", delegate.getLanguage(), false,
              context.getComponentInstanceId(), context.getUser().getId(), albumId);
      pageContext.setEncoding("UTF-8");
      pageContext.setObjectId(mediaId);
      delegate.updateForm(mediaId, pageContext);
    }

    if (GalleryComponentSettings.isPdcEnabled(getMedia().getComponentInstanceId())) {
      // Insert content manager of the media
      int silverContentId = getGalleryContentManager()
          .createSilverContent(context.getConnection(), getMedia(), context.getUser().getId());
      getMedia().setSilverpeasContentId(Integer.toString(silverContentId));

      // Persists pdc classification
      classifyMediaContent();
    }
  }

  private void classifyMediaContent() {
    if (delegate.isHeaderData()) {
      List<PdcPosition> pdcPositions = delegate.getHeaderData().getPdcPositions();
      if (CollectionUtil.isNotEmpty(pdcPositions)) {
        PdcClassification curClassification =
            aPdcClassificationOfContent(getMedia().getSilverpeasContentId(),
                getMedia().getInstanceId()).withPositions(pdcPositions);
        if (!curClassification.isEmpty()) {
          PdcClassificationService service =
              PdcServiceFactory.getFactory().getPdcClassificationService();
          curClassification.ofContent(getMedia().getSilverpeasContentId());
          service.classifyContent(getMedia(), curClassification);
        }
      }
    }
  }
}
