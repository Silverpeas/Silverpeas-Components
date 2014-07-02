/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package com.silverpeas.gallery.model;

import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PhotoTest {

  @Test
  public void justInstancedTest() {
    Photo photo = new Photo();
    assertThat(photo.getType(), is(MediaType.Photo));
    assertThat(photo.getResolutionW(), is(0));
    assertThat(photo.getResolutionH(), is(0));
    assertThat(photo.getMetaDataProperties(), hasSize(0));
    assertThat(photo.getThumbnailUrl(MediaResolution.TINY),
        is("/silverpeas/gallery/jsp/icons/notAvailable_fr" +
            MediaResolution.TINY.getThumbnailSuffix()));
  }

  @Test
  public void justCreatedTest() {
    Photo photo = defaultPhoto();
    assertDefaultPhoto(photo);
  }

  @Test
  public void previewable() {
    Photo photo = defaultPhoto();

    assertThat(photo.isPreviewable(), is(false));
    assertThat(photo.getThumbnailUrl(MediaResolution.MEDIUM),
        is("/silverpeas/gallery/jsp/icons/notAvailable_fr" +
            MediaResolution.MEDIUM.getThumbnailSuffix()));

    photo.setFileName("image.jpg");

    assertThat(photo.isPreviewable(), is(true));
    assertThat(photo.getThumbnailUrl(MediaResolution.LARGE),
        is("/silverpeas/FileServer/mediaId" + MediaResolution.LARGE.getThumbnailSuffix() +
            "?ComponentId=instanceId&SourceFile" +
            "=mediaId" + MediaResolution.LARGE.getThumbnailSuffix() +
            "&MimeType=image/jpeg&Directory=imagemediaId"));
  }

  private Photo defaultPhoto() {
    Photo photo = new Photo();
    MediaPK mediaPK = new MediaPK("mediaId", "instanceId");
    photo.setMediaPK(mediaPK);
    photo.setFileName("/FileName");
    photo.setFileSize(1024);
    photo.setFileMimeType("image/jpeg");
    photo.setResolutionW(800);
    photo.setResolutionH(600);
    photo.addMetaData(new MetaData("ok").setProperty("metadata"));
    assertDefaultPhoto(photo);
    return photo;
  }

  private void assertDefaultPhoto(Photo photo) {
    assertThat(photo.getType(), is(MediaType.Photo));
    assertThat(photo.getWorkspaceSubFolderName(), is("imagemediaId"));
    assertThat(photo.getResolutionW(), is(800));
    assertThat(photo.getResolutionH(), is(600));
    assertThat(photo.getMetaDataProperties(), hasSize(1));
    assertThat(photo.getMetaData(photo.getMetaDataProperties().iterator().next()).getValue(),
        is("ok"));
    assertThat(photo.getThumbnailUrl(MediaResolution.PREVIEW),
        is("/silverpeas/gallery/jsp/icons/notAvailable_fr" +
            MediaResolution.PREVIEW.getThumbnailSuffix()));
  }
}