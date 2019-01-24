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

import org.silverpeas.components.gallery.dao.MediaDAO;
import org.silverpeas.components.gallery.model.AlbumMedia;
import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.notification.AlbumMediaEventNotifier;
import org.silverpeas.components.gallery.process.AbstractGalleryDataProcess;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;

import java.util.Collection;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Process to delete a media from Database
 * @author Yohann Chastagnier
 */
public class GalleryDeleteMediaDataProcess extends AbstractGalleryDataProcess {

  private Collection<String> albumIds;

  /**
   * Default hidden constructor
   * @param media
   */
  protected GalleryDeleteMediaDataProcess(final Media media) {
    super(media);
  }

  /**
   * Gets an instance
   * @param media
   * @return
   */
  public static GalleryDeleteMediaDataProcess getInstance(final Media media) {
    return new GalleryDeleteMediaDataProcess(media);
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
    // Gets the albums
    albumIds = MediaDAO.getAlbumIdsOf(getMedia());

    // Deletes the media
    MediaDAO.deleteMedia(getMedia());

    // Delete form data
    removeXMLContentOfMedia(getMedia().getId(), context);

    // Supprimer les commentaires
    final MediaPK mediaPK = getMedia().getMediaPK();
    CommentServiceProvider.getCommentService()
        .deleteAllCommentsOnPublication(getMedia().getContributionType(), mediaPK);

    // Supprime le silverObject correspond
    getGalleryContentManager().deleteSilverContent(getMedia());
  }

  /**
   * Centralized processing
   * @param mediaId
   * @param context
   */
  private void removeXMLContentOfMedia(final String mediaId,
      final ProcessExecutionContext context) {
    try {
      final String xmlFormName = getXMLFormName(context);
      if (isDefined(xmlFormName)) {
        final String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
        final PublicationTemplate pubTemplate =
            getPublicationTemplateManager().getPublicationTemplate(
                context.getComponentInstanceId() + ":" + xmlFormShortName);

        final RecordSet set = pubTemplate.getRecordSet();
        final DataRecord data = set.getRecord(mediaId);
        if (data != null) {
          set.delete(data.getId());
        }
      }
    } catch (final PublicationTemplateException | FormException e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public void onSuccessful() throws Exception {
    super.onSuccessful();
    final AlbumMediaEventNotifier notifier = AlbumMediaEventNotifier.get();
    for (final String albumId : albumIds) {
      notifier.notifyEventOn(ResourceEvent.Type.DELETION, new AlbumMedia(albumId, getMedia()));
    }
  }
}
