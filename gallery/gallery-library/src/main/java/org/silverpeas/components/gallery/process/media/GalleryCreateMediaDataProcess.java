/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.gallery.process.media;

import org.silverpeas.components.gallery.GalleryComponentSettings;
import org.silverpeas.components.gallery.delegate.MediaDataCreateDelegate;
import org.silverpeas.components.gallery.model.AlbumMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.notification.AlbumMediaEventNotifier;
import org.silverpeas.components.gallery.process.AbstractGalleryDataProcess;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.util.CollectionUtil;

import java.util.List;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

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

  /*
   * (non-Javadoc)
   *
   * AbstractGalleryDataProcess#processData(com.silverpeas.gallery
   * .process.GalleryProcessExecutionContext, ProcessSession)
   */
  @Override
  protected void processData(final ProcessExecutionContext context,
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
      int silverContentId =
          getGalleryContentManager().createSilverContent(getMedia(), context.getUser().getId());
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
            aPdcClassificationOfContent(getMedia()).withPositions(pdcPositions);
        curClassification.classifyContent(getMedia());
      }
    }
  }

  @Override
  public void onSuccessful() throws Exception {
    super.onSuccessful();
    final AlbumMediaEventNotifier notifier = AlbumMediaEventNotifier.get();
    notifier.notifyEventOn(ResourceEvent.Type.CREATION, new AlbumMedia(albumId, getMedia()));
  }
}
