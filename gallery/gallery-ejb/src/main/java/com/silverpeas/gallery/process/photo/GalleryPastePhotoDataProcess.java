/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import org.silverpeas.process.session.Session;

import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.form.record.IdentifiedRecordTemplate;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
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

  private final PhotoPK fromPhotoPk;
  private final boolean isCutted;

  private boolean isSameComponentInstanceDestination = true;
  private ForeignPK fromForeignPK = null;
  private ForeignPK toForeignPK = null;
  private PhotoPK toPhotoPK = null;

  /**
   * Default hidden constructor
   * @param photo
   * @param albumId
   * @param fromPhotoPk
   * @param isCutted
   * @return
   */
  public static GalleryPastePhotoDataProcess getInstance(final PhotoDetail photo,
      final String albumId, final PhotoPK fromPhotoPk, final boolean isCutted) {
    return new GalleryPastePhotoDataProcess(photo, albumId, fromPhotoPk, isCutted);
  }

  /**
   * Default hidden constructor
   * @param photo
   * @param albumId
   * @param fromPhotoPk
   * @param isCutted
   */
  protected GalleryPastePhotoDataProcess(final PhotoDetail photo, final String albumId,
      final PhotoPK fromPhotoPk, final boolean isCutted) {
    super(photo);
    this.albumId = albumId;
    this.fromPhotoPk = fromPhotoPk;
    this.isCutted = isCutted;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.gallery.process.AbstractGalleryDataProcess#processData(com.silverpeas.gallery
   * .process.GalleryProcessExecutionContext, org.silverpeas.process.session.Session)
   */
  @Override
  protected void processData(final GalleryProcessExecutionContext context, final Session session)
      throws Exception {

    // Initializing variables
    isSameComponentInstanceDestination =
        fromPhotoPk.getInstanceId().equals(context.getComponentInstanceId());
    fromForeignPK = new ForeignPK(getPhoto().getId(), fromPhotoPk.getInstanceId());

    // If the destination application is different from the original, then update destination
    // information and user data
    if (!isSameComponentInstanceDestination) {
      getPhoto().getPhotoPK().setComponentName(context.getComponentInstanceId());
      getPhoto().setAlbumId(albumId);
    }

    // If the paste action is copy and paste (not cut and paste), then create the new photo
    if (!isCutted) {
      createPhoto(albumId);
    } else {
      updatePhoto();
      if (isSameComponentInstanceDestination) {

        // Move into the same application
        updatePhotoPath(context.getComponentInstanceId(), albumId);

      } else {

        // Updating repository data
        updatePhotoPath(fromPhotoPk.getInstanceId(), albumId);
      }
    }

    // Initializing variables after photo creation
    toForeignPK = new ForeignPK(getPhoto().getId(), context.getComponentInstanceId());
    toPhotoPK = new PhotoPK(getPhoto().getId(), context.getComponentInstanceId());

    // Form
    pasteXmlForm(context);
  }

  /**
   * Paste XML Form
   * @param context
   * @throws Exception
   */
  private void pasteXmlForm(final GalleryProcessExecutionContext context) throws Exception {
    if (!isCutted || !isSameComponentInstanceDestination) {
      try {
        final String xmlFormName = getXMLFormName();
        if (isDefined(xmlFormName)) {

          // if XMLForm
          final String xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
          getPublicationTemplateManager().addDynamicPublicationTemplate(
              context.getComponentInstanceId() + ":" + xmlFormShortName, xmlFormShortName + ".xml");

          // get xmlContent to paste
          final PublicationTemplate pubTemplateFrom =
              getPublicationTemplateManager().getPublicationTemplate(
                  fromPhotoPk.getInstanceId() + ":" + xmlFormShortName);
          final IdentifiedRecordTemplate recordTemplateFrom =
              (IdentifiedRecordTemplate) pubTemplateFrom.getRecordSet().getRecordTemplate();

          final PublicationTemplate pubTemplate =
              getPublicationTemplateManager().getPublicationTemplate(
                  context.getComponentInstanceId() + ":" + xmlFormShortName);
          final IdentifiedRecordTemplate recordTemplate =
              (IdentifiedRecordTemplate) pubTemplate.getRecordSet().getRecordTemplate();

          // paste xml content
          getGenericRecordSetManager().cloneRecord(recordTemplateFrom, fromPhotoPk.getId(),
              recordTemplate, getPhoto().getId(), null);
        }
      } catch (final PublicationTemplateException e) {
        SilverTrace.info("gallery", "GalleryPastePhotoDataProcess.processPasteCommons()",
            "gallery.DIFERENT_FORM_COMPONENT", e);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.AbstractProcess#onSuccessful(org.silverpeas.process.management.
   * ProcessExecutionContext, org.silverpeas.process.session.Session)
   */
  @Override
  public void onSuccessful(final GalleryProcessExecutionContext context, final Session session)
      throws Exception {
    super.onSuccessful(context, session);

    // Commons
    if (!isCutted || !isSameComponentInstanceDestination) {
      processPasteCommons(context);
    }

    SilverTrace.info("gallery", "GalleryPastePhotoDataProcess.onSuccessful()",
        "root.MSG_GEN_PARAM_VALUE", "photo = " + getPhoto().toString() + " toPK = " +
            getPhoto().getPhotoPK().toString());
  }

  /**
   * Centralized method
   * @throws Exception
   */
  private void processPasteCommons(final GalleryProcessExecutionContext context) throws Exception {

    // Paste positions on Pdc
    final int fromSilverObjectId = getGalleryBm().getSilverObjectId(fromPhotoPk);
    final int toSilverObjectId = getGalleryBm().getSilverObjectId(toPhotoPK);

    PdcServiceFactory
        .getFactory()
        .getPdcManager()
        .copyPositions(fromSilverObjectId, fromPhotoPk.getInstanceId(), toSilverObjectId,
            context.getComponentInstanceId());

    // move comments
    CommentServiceFactory.getFactory().getCommentService()
        .moveAndReindexComments(PhotoDetail.getResourceType(), fromForeignPK, toForeignPK);
  }
}
