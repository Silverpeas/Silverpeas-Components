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
import com.silverpeas.gallery.dao.PhotoDAO;
import com.silverpeas.gallery.delegate.PhotoDataCreateDelegate;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.util.StringUtil;

/**
 * Process to create a photo in Database
 * @author Yohann Chastagnier
 */
public class GalleryCreatePhotoDataProcess extends AbstractGalleryDataProcess {

  private final String albumId;

  /** Delegate in charge of creating photo data */
  private final PhotoDataCreateDelegate delegate;

  /**
   * Gets an instance
   * @param photo
   * @param delegate
   * @return
   */
  public static GalleryCreatePhotoDataProcess getInstance(final PhotoDetail photo,
      final String albumId, final PhotoDataCreateDelegate delegate) {
    return new GalleryCreatePhotoDataProcess(photo, albumId, delegate);
  }

  /**
   * Default hidden constructor
   * @param photo
   * @param delegate
   */
  protected GalleryCreatePhotoDataProcess(final PhotoDetail photo, final String albumId,
      final PhotoDataCreateDelegate delegate) {
    super(photo);
    this.delegate = delegate;
    this.albumId = albumId;
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

    // Photo
    if (delegate.isHeaderData()) {
      delegate.updateHeader(getPhoto());
    }

    createPhoto(albumId);

    // Persists form data
    if (delegate.isForm()) {
      final String photoId = getPhoto().getId();
      final PagesContext pageContext =
          new PagesContext("photoForm", "0", delegate.getLanguage(), false,
              context.getComponentInstanceId(), context.getUser().getId(), albumId);
      pageContext.setEncoding("UTF-8");
      pageContext.setObjectId(photoId);
      delegate.updateForm(photoId, pageContext);
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

    // Save data that have been computed during other processes
    if (!StringUtil.isDefined(getPhoto().getTitle())) {
      getPhoto().setTitle(getPhoto().getImageName());
    }
    PhotoDAO.updatePhoto(context.getConnection(), getPhoto());
  }
}
