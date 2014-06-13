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
package com.silverpeas.gallery.process.photo;

import static com.silverpeas.util.StringUtil.isDefined;

import com.silverpeas.gallery.model.MediaPK;
import org.silverpeas.process.session.ProcessSession;

import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.form.record.IdentifiedRecordTemplate;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Process to paste a photo in Database
 * @author Yohann Chastagnier
 */
public class GalleryPastePhotoDataProcess extends AbstractGalleryDataProcess {

  /** Id of the album (Node) */
  private final String albumId;

  private final MediaPK fromMediaPk;
  private final boolean isCutted;

  private boolean isSameComponentInstanceDestination = true;
  private ForeignPK fromForeignPK = null;
  private ForeignPK toForeignPK = null;
  private MediaPK toMediaPK = null;

  /**
   * Default hidden constructor
   * @param photo
   * @param albumId
   * @param fromMediaPk
   * @param isCutted
   * @return
   */
  public static GalleryPastePhotoDataProcess getInstance(final PhotoDetail photo,
      final String albumId, final MediaPK fromMediaPk, final boolean isCutted) {
    return new GalleryPastePhotoDataProcess(photo, albumId, fromMediaPk, isCutted);
  }

  /**
   * Default hidden constructor
   * @param photo
   * @param albumId
   * @param fromMediaPk
   * @param isCutted
   */
  protected GalleryPastePhotoDataProcess(final PhotoDetail photo, final String albumId,
      final MediaPK fromMediaPk, final boolean isCutted) {
    super(photo);
    this.albumId = albumId;
    this.fromMediaPk = fromMediaPk;
    this.isCutted = isCutted;
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

    // Initializing variables
    isSameComponentInstanceDestination =
        fromMediaPk.getInstanceId().equals(context.getComponentInstanceId());
    fromForeignPK = new ForeignPK(getPhoto().getId(), fromMediaPk.getInstanceId());

    // If the destination application is different from the original, then update destination
    // information and user data
    if (!isSameComponentInstanceDestination) {
      getPhoto().getMediaPK().setComponentName(context.getComponentInstanceId());
    }

    // If the paste action is copy and paste (not cut and paste), then create the new photo
    if (!isCutted) {
      createPhoto(albumId, context);
    } else {
      updatePhoto(true, context);
      if (isSameComponentInstanceDestination) {

        // Move into the same application
        updatePhotoPath(context.getComponentInstanceId(), albumId, context);

      } else {

        // Updating repository data
        updatePhotoPath(fromMediaPk.getInstanceId(), albumId, context);
      }
    }

    // Initializing variables after photo creation
    toForeignPK = new ForeignPK(getPhoto().getId(), context.getComponentInstanceId());
    toMediaPK = new MediaPK(getPhoto().getId(), context.getComponentInstanceId());

    // Commons
    processPasteCommons(context);

    SilverTrace.info("gallery", "GalleryPastePhotoDataProcess.onSuccessful()",
        "root.MSG_GEN_PARAM_VALUE", "photo = " + getPhoto().toString() + " toPK = " +
            getPhoto().getMediaPK().toString());
  }

  /**
   * Centralized method
   * @throws Exception
   */
  private void processPasteCommons(final GalleryProcessExecutionContext context) throws Exception {
    if (!isCutted || !isSameComponentInstanceDestination) {
      // Paste positions on Pdc
      final int fromSilverObjectId = getGalleryBm().getSilverObjectId(fromMediaPk);
      final int toSilverObjectId = getGalleryBm().getSilverObjectId(toMediaPK);

      PdcServiceFactory
          .getFactory()
          .getPdcManager()
          .copyPositions(fromSilverObjectId, fromMediaPk.getInstanceId(), toSilverObjectId,
              context.getComponentInstanceId());

      // move comments
      CommentServiceFactory.getFactory().getCommentService()
          .moveAndReindexComments(PhotoDetail.getResourceType(), fromForeignPK, toForeignPK);

      // XML Form
      pasteXmlForm(context);
    }
  }

  /**
   * Paste XML Form
   * @param context
   * @throws Exception
   */
  private void pasteXmlForm(final GalleryProcessExecutionContext context) throws Exception {
    if (!isCutted || !isSameComponentInstanceDestination) {
      try {
        final String xmlFormName = getXMLFormName(context);
        if (isDefined(xmlFormName)) {

          // if XMLForm
          final String xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
          getPublicationTemplateManager().addDynamicPublicationTemplate(
              context.getComponentInstanceId() + ":" + xmlFormShortName, xmlFormShortName + ".xml");

          // get xmlContent to paste
          final PublicationTemplate pubTemplateFrom =
              getPublicationTemplateManager().getPublicationTemplate(
                  fromMediaPk.getInstanceId() + ":" + xmlFormShortName);
          final IdentifiedRecordTemplate recordTemplateFrom =
              (IdentifiedRecordTemplate) pubTemplateFrom.getRecordSet().getRecordTemplate();

          final PublicationTemplate pubTemplate =
              getPublicationTemplateManager().getPublicationTemplate(
                  context.getComponentInstanceId() + ":" + xmlFormShortName);
          final IdentifiedRecordTemplate recordTemplate =
              (IdentifiedRecordTemplate) pubTemplate.getRecordSet().getRecordTemplate();

          // paste xml content
          getGenericRecordSetManager().cloneRecord(recordTemplateFrom, fromMediaPk.getId(),
              recordTemplate, getPhoto().getId(), null);
        }
      } catch (final PublicationTemplateException e) {
        SilverTrace.info("gallery", "GalleryPastePhotoDataProcess.processPasteCommons()",
            "gallery.DIFERENT_FORM_COMPONENT", e);
      }
    }
  }
}
