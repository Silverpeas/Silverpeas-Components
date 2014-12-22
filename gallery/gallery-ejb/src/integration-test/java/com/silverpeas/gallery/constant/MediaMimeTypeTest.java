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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.constant;

import com.silverpeas.gallery.GalleryComponentSettings;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.BasicWarBuilder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class MediaMimeTypeTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(MediaMimeTypeTest.class).testFocusedOn(warBuilder -> {
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:pdc");
      warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:publication");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:formtemplate");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:searchengine");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:comment");
      warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
      warBuilder.addMavenDependencies("org.apache.tika:tika-core", "org.apache.tika:tika-parsers");
      warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
      warBuilder.addAsResource("org/silverpeas/gallery/multilang/galleryBundle.properties");
      warBuilder.addAsResource("org/silverpeas/gallery/settings/gallerySettings.properties");
      warBuilder.addAsResource("org/silverpeas/util/attachment/mime_types.properties");
      warBuilder.addClasses(GalleryComponentSettings.class);
      warBuilder.addPackages(true, "com.silverpeas.gallery.constant");
    }).build();
  }

  @Test
  public void verifyMediaMimeTypes() {
    Set<MediaMimeType> validTypes = new HashSet<>();
    validTypes.addAll(MediaMimeType.PHOTOS);
    validTypes.addAll(MediaMimeType.VIDEOS);
    validTypes.addAll(MediaMimeType.SOUNDS);
    assertThat(MediaMimeType.values().length - 1, is(validTypes.size()));


    assertThat(MediaMimeType.ERROR, is(MediaMimeType.fromFile(getFile("tIfs"))));
    assertThat(MediaMimeType.ERROR, is(MediaMimeType.fromFile(getFile("flvs"))));
    assertThat(MediaMimeType.fromMimeType("image/unknown"), is(MediaMimeType.ERROR));

    // PHOTOS
    assertThat(MediaMimeType.BMP, is(MediaMimeType.fromFile(getFile("bmp"))));
    assertThat(MediaMimeType.BMP.isReadablePhoto(), is(true));
    assertThat(MediaMimeType.BMP.isPreviewablePhoto(), is(true));
    assertThat(MediaMimeType.BMP.isIPTCCompliant(), is(false));
    assertThat(MediaMimeType.BMP.isSupportedPhotoType(), is(true));
    assertThat(MediaMimeType.BMP.isSupportedVideoType(), is(false));
    assertThat(MediaMimeType.BMP.isSupportedSoundType(), is(false));
    assertThat(MediaMimeType.JPG, is(MediaMimeType.fromFile(getFile("jPg"))));
    assertThat(MediaMimeType.JPG, is(MediaMimeType.fromFile(getFile("jPEg"))));
    assertThat(MediaMimeType.JPG.isReadablePhoto(), is(true));
    assertThat(MediaMimeType.JPG.isPreviewablePhoto(), is(true));
    assertThat(MediaMimeType.JPG.isIPTCCompliant(), is(true));
    assertThat(MediaMimeType.JPG.isSupportedPhotoType(), is(true));
    assertThat(MediaMimeType.JPG.isSupportedVideoType(), is(false));
    assertThat(MediaMimeType.JPG.isSupportedSoundType(), is(false));
    assertThat(MediaMimeType.PNG, is(MediaMimeType.fromFile(getFile("pNg"))));
    assertThat(MediaMimeType.PNG.isReadablePhoto(), is(true));
    assertThat(MediaMimeType.PNG.isPreviewablePhoto(), is(true));
    assertThat(MediaMimeType.PNG.isIPTCCompliant(), is(false));
    assertThat(MediaMimeType.PNG.isSupportedPhotoType(), is(true));
    assertThat(MediaMimeType.PNG.isSupportedVideoType(), is(false));
    assertThat(MediaMimeType.PNG.isSupportedSoundType(), is(false));
    assertThat(MediaMimeType.GIF, is(MediaMimeType.fromFile(getFile("gif"))));
    assertThat(MediaMimeType.GIF.isReadablePhoto(), is(true));
    assertThat(MediaMimeType.GIF.isPreviewablePhoto(), is(true));
    assertThat(MediaMimeType.GIF.isIPTCCompliant(), is(true));
    assertThat(MediaMimeType.GIF.isSupportedPhotoType(), is(true));
    assertThat(MediaMimeType.GIF.isSupportedVideoType(), is(false));
    assertThat(MediaMimeType.GIF.isSupportedSoundType(), is(false));
    assertThat(MediaMimeType.TIFF, is(MediaMimeType.fromFile(getFile("tIff"))));
    assertThat(MediaMimeType.TIFF, is(MediaMimeType.fromFile(getFile("tIf"))));
    assertThat(MediaMimeType.TIFF.isReadablePhoto(), is(false));
    assertThat(MediaMimeType.TIFF.isPreviewablePhoto(), is(false));
    assertThat(MediaMimeType.TIFF.isIPTCCompliant(), is(true));
    assertThat(MediaMimeType.TIFF.isSupportedPhotoType(), is(true));
    assertThat(MediaMimeType.TIFF.isSupportedVideoType(), is(false));
    assertThat(MediaMimeType.TIFF.isSupportedSoundType(), is(false));

    assertThat(MediaMimeType.fromMimeType("image/bmp"), is(MediaMimeType.BMP));
    assertThat(MediaMimeType.fromMimeType("image/png"), is(MediaMimeType.PNG));
    assertThat(MediaMimeType.fromMimeType("image/jpeg"), is(MediaMimeType.JPG));
    assertThat(MediaMimeType.fromMimeType("image/pjpeg"), is(MediaMimeType.JPG));
    assertThat(MediaMimeType.fromMimeType("image/gif"), is(MediaMimeType.GIF));
    assertThat(MediaMimeType.fromMimeType("image/tiff"), is(MediaMimeType.TIFF));

    // IMAGES
    assertThat(MediaMimeType.MP4, is(MediaMimeType.fromFile(getFile("mP4"))));
    assertThat(MediaMimeType.MP4.isReadablePhoto(), is(false));
    assertThat(MediaMimeType.MP4.isPreviewablePhoto(), is(false));
    assertThat(MediaMimeType.MP4.isIPTCCompliant(), is(false));
    assertThat(MediaMimeType.MP4.isSupportedPhotoType(), is(false));
    assertThat(MediaMimeType.MP4.isSupportedVideoType(), is(true));
    assertThat(MediaMimeType.MP4.isSupportedSoundType(), is(false));
    assertThat(MediaMimeType.FLV, is(MediaMimeType.fromFile(getFile("flV"))));
    assertThat(MediaMimeType.FLV.isReadablePhoto(), is(false));
    assertThat(MediaMimeType.FLV.isPreviewablePhoto(), is(false));
    assertThat(MediaMimeType.FLV.isIPTCCompliant(), is(false));
    assertThat(MediaMimeType.FLV.isSupportedPhotoType(), is(false));
    assertThat(MediaMimeType.FLV.isSupportedVideoType(), is(true));
    assertThat(MediaMimeType.FLV.isSupportedSoundType(), is(false));

    assertThat(MediaMimeType.fromMimeType("video/mp4"), is(MediaMimeType.MP4));
    assertThat(MediaMimeType.fromMimeType("video/x-flv"), is(MediaMimeType.FLV));

    // SOUND
    assertThat(MediaMimeType.MP3, is(MediaMimeType.fromFile(getFile("mp3"))));
    assertThat(MediaMimeType.MP3.isReadablePhoto(), is(false));
    assertThat(MediaMimeType.MP3.isPreviewablePhoto(), is(false));
    assertThat(MediaMimeType.MP3.isIPTCCompliant(), is(false));
    assertThat(MediaMimeType.MP3.isSupportedPhotoType(), is(false));
    assertThat(MediaMimeType.MP3.isSupportedVideoType(), is(false));
    assertThat(MediaMimeType.MP3.isSupportedSoundType(), is(true));

    assertThat(MediaMimeType.fromMimeType("audio/x-mpeg"), is(MediaMimeType.MP3));
  }

  private File getFile(String extension) {
    return new File("file." + extension);
  }
}