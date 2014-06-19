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

import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.gallery.dao.MediaDAO;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.process.session.ProcessSession;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * Process to delete a photo from Database
 * @author Yohann Chastagnier
 */
public class GalleryDeletePhotoDataProcess extends AbstractGalleryDataProcess {

  /**
   * Gets an instance
   * @param photo
   * @return
   */
  public static GalleryDeletePhotoDataProcess getInstance(final PhotoDetail photo) {
    return new GalleryDeletePhotoDataProcess(photo);
  }

  /**
   * Default hidden constructor
   * @param photo
   */
  protected GalleryDeletePhotoDataProcess(final PhotoDetail photo) {
    super(photo);
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
    MediaDAO.deleteMedia(context.getConnection(), getPhoto().getPhoto());

    // Delete form data
    removeXMLContentOfPhoto(getPhoto().getId(), context);

    // Supprimer les commentaires
    CommentServiceFactory.getFactory().getCommentService()
        .deleteAllCommentsOnPublication(PhotoDetail.getResourceType(), getPhoto().getMediaPK());

    // Supprime le silverObject correspond
    getGalleryContentManager()
        .deleteSilverContent(context.getConnection(), getPhoto().getMediaPK());
  }

  /**
   * Centralized processing
   * @param photoId
   * @param context
   */
  private void removeXMLContentOfPhoto(final String photoId,
      final GalleryProcessExecutionContext context) {
    try {
      final String xmlFormName = getXMLFormName(context);
      if (isDefined(xmlFormName)) {
        final String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
        final PublicationTemplate pubTemplate =
            getPublicationTemplateManager().getPublicationTemplate(
                context.getComponentInstanceId() + ":" + xmlFormShortName);

        final RecordSet set = pubTemplate.getRecordSet();
        final DataRecord data = set.getRecord(photoId);
        set.delete(data);
      }
    } catch (final PublicationTemplateException e) {
      throw new GalleryRuntimeException("GallerySessionController.removeXMLContentOfPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    } catch (final FormException e) {
      throw new GalleryRuntimeException("GallerySessionController.removeXMLContentOfPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.AbstractProcess#onSuccessful()
   */
  @Override
  public void onSuccessful() throws Exception {
    super.onSuccessful();

    /*
     * TODO - YCH - JMS ?
     * AttachmentController
     * .deleteAttachment(AttachmentController.searchAttachmentByCustomerPK(new ForeignPK(
     * photoId, context.getComponentInstanceId())));
     */
  }
}
