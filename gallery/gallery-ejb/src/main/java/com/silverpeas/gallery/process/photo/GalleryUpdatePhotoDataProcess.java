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

import org.silverpeas.process.session.Session;

import com.silverpeas.form.PagesContext;
import com.silverpeas.gallery.delegate.PhotoDataUpdateDelegate;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Process to update a photo in Database
 * @author Yohann Chastagnier
 */
public class GalleryUpdatePhotoDataProcess extends AbstractGalleryDataProcess {

  /** Delegate in charge of updating photo data */
  private final PhotoDataUpdateDelegate delegate;

  /**
   * Gets an instance
   * @param photo
   * @param delegate
   * @return
   */
  public static GalleryUpdatePhotoDataProcess getInstance(final PhotoDetail photo,
      final PhotoDataUpdateDelegate delegate) {
    return new GalleryUpdatePhotoDataProcess(photo, delegate);
  }

  /**
   * Default hidden constructor
   * @param photo
   * @param delegate
   */
  protected GalleryUpdatePhotoDataProcess(final PhotoDetail photo,
      final PhotoDataUpdateDelegate delegate) {
    super(photo);
    this.delegate = delegate;
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

    SilverTrace.info("gallery", "GalleryUpdatePhotoDataProcess.process()",
        "root.MSG_GEN_ENTER_METHOD", "PhotoPK = " + getPhoto().toString());

    // Sets functional data photo
    if (delegate != null) {
      if (delegate.isHeaderData()) {
        delegate.updateHeader(getPhoto());
      }

      // Persists form data
      if (delegate.isForm()) {
        final String photoId = getPhoto().getId();
        final PagesContext pageContext =
            new PagesContext("photoForm", "0", delegate.getLanguage(), false,
                context.getComponentInstanceId(), context.getUser().getId(), delegate.getAlbumId());
        pageContext.setEncoding("UTF-8");
        pageContext.setObjectId(photoId);
        if (delegate.isSkipEmptyValues()) {
          pageContext.setUpdatePolicy(PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES);
        }
        delegate.updateForm(photoId, pageContext);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.AbstractProcess#onSuccessful(org.silverpeas.process.management.
   * ProcessExecutionContext, org.silverpeas.process.session.Session)
   */
  @Override
  public void onSuccessful(final GalleryProcessExecutionContext processExecutionContext,
      final Session session) throws Exception {
    super.onSuccessful(processExecutionContext, session);

    // Update photo
    updatePhoto();
  }
}
