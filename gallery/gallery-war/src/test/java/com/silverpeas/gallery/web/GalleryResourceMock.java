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
package com.silverpeas.gallery.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.Path;

import static com.silverpeas.gallery.constant.GalleryResourceURIs.GALLERY_BASE_URI;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(GALLERY_BASE_URI + "/{componentInstanceId}")
@Authorized
public class GalleryResourceMock extends GalleryResource {

  private GalleryBm galleryBmMock = null;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.gallery.web.AbstractGalleryResource#getGalleryBm()
   */
  @Override
  protected GalleryBm getGalleryBm() {
    try {
      if (galleryBmMock == null) {
        galleryBmMock = mock(GalleryBm.class);

        // getAlbum
        when(galleryBmMock.getAlbum(any(NodePK.class)))
            .thenAnswer(
                new Answer<AlbumDetail>() {

                  @Override
                  public AlbumDetail answer(final InvocationOnMock invocation) throws Throwable {
                    final NodePK nodePk = (NodePK) invocation.getArguments()[0];
                    if (nodePk == null || !"3".equals(nodePk.getId())) {
                      return null;
                    }
                    return AlbumBuilder.getAlbumBuilder()
                        .buildAlbum(nodePk.getId()).addMedia(PhotoBuilder.getPhotoBuilder()
                            .buildPhoto("7", nodePk.getComponentName()));
                  }
                });

        // getPhoto
        when(galleryBmMock.getMedia(any(MediaPK.class))).thenAnswer(new Answer<Media>() {

          @Override
          public Media answer(final InvocationOnMock invocation) throws Throwable {
            final MediaPK mediaPk = (MediaPK) invocation.getArguments()[0];
            if (mediaPk == null || !"7".equals(mediaPk.getId())) {
              return null;
            }
            return PhotoBuilder.getPhotoBuilder().buildPhoto(mediaPk.getId(),
                mediaPk.getComponentName());
          }
        });
      }
      return galleryBmMock;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.gallery.web.AbstractGalleryResource#verifyViewAllPhotoAuthorized(com.silverpeas
   * .gallery.model.PhotoDetail)
   */
  @Override
  protected void verifyUserMediaAccess(final Media media) {
    // Nothing to do.
  }
}
