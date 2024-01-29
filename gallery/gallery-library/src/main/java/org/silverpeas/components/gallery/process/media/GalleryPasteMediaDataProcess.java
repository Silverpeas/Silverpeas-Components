/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.process.media;

import org.silverpeas.components.gallery.dao.MediaDAO;
import org.silverpeas.components.gallery.model.AlbumMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.notification.AlbumMediaEventNotifier;
import org.silverpeas.components.gallery.process.AbstractGalleryDataProcess;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.pdc.PdcServiceProvider;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.sql.SQLException;
import java.util.Collection;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Process to paste a media in Database
 * @author Yohann Chastagnier
 */
public class GalleryPasteMediaDataProcess extends AbstractGalleryDataProcess {

  /**
   * Id of the album (Node)
   */
  private final String albumId;
  private Collection<String> albumIdsForDeletion;

  private final MediaPK fromMediaPk;
  private final boolean isCutted;

  private boolean isSameComponentInstanceDestination = true;
  private ResourceReference fromResourceReference = null;
  private ResourceReference toResourceReference = null;
  private MediaPK toMediaPK = null;
  private final Media mediaBeforeChanges;

  /**
   * Default hidden constructor
   * @param media
   * @param albumId
   * @param fromMediaPk
   * @param isCutted
   */
  protected GalleryPasteMediaDataProcess(final Media media, final String albumId,
      final MediaPK fromMediaPk, final boolean isCutted) {
    super(media);
    this.albumId = albumId;
    this.fromMediaPk = fromMediaPk;
    this.isCutted = isCutted;
    this.mediaBeforeChanges = media.getCopy();
  }

  /**
   * Default hidden constructor
   * @param media
   * @param albumId
   * @param fromMediaPk
   * @param isCutted
   * @return
   */
  public static GalleryPasteMediaDataProcess getInstance(final Media media, final String albumId,
      final MediaPK fromMediaPk, final boolean isCutted) {
    return new GalleryPasteMediaDataProcess(media, albumId, fromMediaPk, isCutted);
  }

  /*
   * (non-Javadoc)
   *
   * AbstractGalleryDataProcess#processData(com.silverpeas.gallery
   * .process.ProcessExecutionContext, ProcessSession)
   */
  @Override
  protected void processData(final ProcessExecutionContext context,
      final ProcessSession session) throws Exception {
    // Gets the albums
    albumIdsForDeletion = MediaDAO.getAlbumIdsOf(mediaBeforeChanges);
    albumIdsForDeletion.remove(albumId);

    // Initializing variables
    isSameComponentInstanceDestination =
        fromMediaPk.getInstanceId().equals(context.getComponentInstanceId());
    fromResourceReference = new ResourceReference(getMedia().getId(), fromMediaPk.getInstanceId());

    // If the destination application is different from the original, then update destination
    // information and user data
    if (!isSameComponentInstanceDestination) {
      getMedia().getMediaPK().setComponentName(context.getComponentInstanceId());
    }

    // If the paste action is copy and paste (not cut and paste), then create the new media
    if (!isCutted) {
      createMedia(albumId, context);
    } else {
      updateMedia(true, context);
      if (isSameComponentInstanceDestination) {

        // Move into the same application
        moveMediaPath(context.getComponentInstanceId(), albumId, context);

      } else {

        // Updating repository data
        moveMediaPath(fromMediaPk.getInstanceId(), albumId, context);
      }
    }

    // Initializing variables after media creation
    toResourceReference = new ResourceReference(getMedia().getId(), context.getComponentInstanceId());
    toMediaPK = new MediaPK(getMedia().getId(), context.getComponentInstanceId());

    // Commons
    processPasteCommons(context);
  }

  /**
   * Centralizes the media path update
   * @param fromComponentInstanceId
   * @param albumId
   * @param context
   * @throws Exception
   */
  private void moveMediaPath(final String fromComponentInstanceId, final String albumId,
      final ProcessExecutionContext context) throws SQLException {
    getMedia().setComponentInstanceId(fromComponentInstanceId);
    MediaDAO.deleteAllMediaPath(getMedia());
    getMedia().setComponentInstanceId(context.getComponentInstanceId());
    MediaDAO.saveMediaPath(getMedia(), albumId);
  }

  private void processPasteCommons(final ProcessExecutionContext context)
      throws PdcException, FormException {
    if (!isCutted || !isSameComponentInstanceDestination) {
      // Paste positions on Pdc
      final int fromSilverObjectId = getGalleryBm().getSilverObjectId(fromMediaPk);
      final int toSilverObjectId = getGalleryBm().getSilverObjectId(toMediaPK);

      PdcServiceProvider.getPdcManager()
          .copyPositions(fromSilverObjectId, fromMediaPk.getInstanceId(), toSilverObjectId,
              context.getComponentInstanceId());

      // move comments
      CommentServiceProvider.getCommentService()
          .moveAndReindexComments(getMedia().getContributionType(), fromResourceReference,
              toResourceReference);

      // XML Form
      pasteXmlForm(context);
    }
  }

  /**
   * Paste XML Form
   * @param context
   * @throws FormException
   */
  private void pasteXmlForm(final ProcessExecutionContext context) throws FormException {
    if (!isCutted || !isSameComponentInstanceDestination) {
      try {
        final String xmlFormName = getXMLFormName(context);
        if (isDefined(xmlFormName)) {

          // Stopping if no defined xml form is detected
          final String xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
          if (StringUtil.isNotDefined(xmlFormShortName) && "0".equals(xmlFormShortName)) {
            return;
          }

          // Getting the recordset
          GenericRecordSet toRecordset = getPublicationTemplateManager()
              .addDynamicPublicationTemplate(
                  toResourceReference.getInstanceId() + ":" + xmlFormShortName,
                  xmlFormShortName + ".xml");

          PublicationTemplate pubTemplate = getPublicationTemplateManager()
              .getPublicationTemplate(fromMediaPk.getInstanceId() + ":" + xmlFormShortName);
          RecordSet set = pubTemplate.getRecordSet();

          if (!isCutted) {
            set.copy(fromResourceReference, toResourceReference, toRecordset.getRecordTemplate(), null);
          } else {
            set.move(fromResourceReference, toResourceReference, toRecordset.getRecordTemplate());
          }
        }
      } catch (final PublicationTemplateException e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
  }

  @Override
  public void onSuccessful() throws Exception {
    super.onSuccessful();
    final AlbumMediaEventNotifier notifier = AlbumMediaEventNotifier.get();
    if (isCutted) {
      for (final String albumIdForDeletion : albumIdsForDeletion) {
        notifier.notifyEventOn(ResourceEvent.Type.DELETION,
            new AlbumMedia(albumIdForDeletion, mediaBeforeChanges));
      }
    }
    notifier.notifyEventOn(ResourceEvent.Type.CREATION, new AlbumMedia(albumId, getMedia()));
  }
}
