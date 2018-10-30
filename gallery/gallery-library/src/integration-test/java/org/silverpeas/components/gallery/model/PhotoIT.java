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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.model;

import org.apache.commons.io.FilenameUtils;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.gallery.GalleryWarBuilder;
import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.core.io.media.Definition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Arquillian.class)
public class PhotoIT extends AbstractMediaIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(PhotoIT.class).build();
  }

  @Test
  public void justInstancedTest() {
    Photo photo = new Photo();
    assertThat(photo.getType(), Matchers.is(MediaType.Photo));
    assertThat(photo.getDefinition().getWidth(), is(0));
    assertThat(photo.getDefinition().getHeight(), is(0));
    assertThat(photo.getMetaDataProperties(), hasSize(0));
    assertThat(photo.getApplicationThumbnailUrl(MediaResolution.TINY),
        is("/silverpeas/gallery/jsp/icons/notAvailable_fr" +
            MediaResolution.TINY.getThumbnailSuffix() + ".jpg"));
  }

  @Test
  public void justCreatedTest() {
    Photo photo = defaultPhoto();
    assertDefaultPhoto(photo);
  }

  @Test
  public void previewable() {
    Photo photo = defaultPhoto();
    photo.setFileName(null);

    assertThat(photo.isPreviewable(), is(false));
    assertThat(photo.getApplicationThumbnailUrl(MediaResolution.MEDIUM),
        is("/silverpeas/gallery/jsp/icons/notAvailable_fr" +
            MediaResolution.MEDIUM.getThumbnailSuffix() + ".jpg"));

    photo.setFileName("image.jpg");

    assertThat(photo.isPreviewable(), is(true));
    assertThat(photo.getApplicationThumbnailUrl(MediaResolution.LARGE),
        is(GALLERY_REST_WEB_SERVICE_BASE_URI +
            "photos/mediaId/content?_t=1393628400000&resolution=LARGE"));
  }

  private Photo defaultPhoto() {
    Photo photo = new Photo();
    MediaPK mediaPK = new MediaPK("mediaId", "instanceId");
    photo.setMediaPK(mediaPK);
    photo.setFileName("photoFile.jpg");
    photo.setFileSize(1024);
    photo.setFileMimeType(MediaMimeType.JPG);
    photo.setDefinition(Definition.of(800, 600));
    photo.addMetaData(new MetaData("ok").setProperty("metadata"));
    photo.setCreationDate(TODAY);
    assertDefaultPhoto(photo);
    return photo;
  }

  private void assertDefaultPhoto(Photo photo) {
    assertThat(photo.getType(), is(MediaType.Photo));
    assertThat(photo.getWorkspaceSubFolderName(), is("imagemediaId"));
    assertThat(photo.getDefinition().getWidth(), is(800));
    assertThat(photo.getDefinition().getHeight(), is(600));
    assertThat(photo.getMetaDataProperties(), hasSize(1));
    assertThat(photo.getMetaData(photo.getMetaDataProperties().iterator().next()).getValue(),
        is("ok"));
    assertThat(photo.getApplicationThumbnailUrl(MediaResolution.PREVIEW),
        is(GALLERY_REST_WEB_SERVICE_BASE_URI +
            "photos/mediaId/content?_t=1393628400000&resolution=PREVIEW"));
    assertThat(photo.getApplicationOriginalUrl(),
        is(GALLERY_REST_WEB_SERVICE_BASE_URI + "photos/mediaId/content?_t=1393628400000"));
    assertThat(FilenameUtils.normalize(photo.getFile(MediaResolution.ORIGINAL).getPath(), true),
        endsWith("/instanceId/imagemediaId/photoFile.jpg"));
  }
}