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

import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.GalleryWarBuilder;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import org.apache.commons.io.FilenameUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static com.silverpeas.gallery.constant.MediaResolution.*;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
@RunWith(Arquillian.class)
public class SoundTest extends AbstractMediaTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(SoundTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addClasses(GalleryComponentSettings.class);
          warBuilder.addPackages(true, "com.silverpeas.gallery.constant");
          warBuilder.addPackages(true, "com.silverpeas.gallery.model");
          warBuilder.addAsResource("maven.properties");
        }).build();
  }

  @Test
  public void justInstancedTest() {
    Sound sound = new Sound();
    assertThat(sound.getType(), is(MediaType.Sound));
    assertThat(sound.getBitrate(), is(0L));
    assertThat(sound.getDuration(), is(0L));
  }

  @Test
  public void justCreatedTest() {
    Sound sound = defaultSound();
    assertDefaultSound(sound);

    Map<MediaResolution, String> expected = new HashMap<>();
    expected.put(TINY, "/silverpeas/gallery/jsp/icons/sound_66x50.png");
    expected.put(SMALL, "/silverpeas/gallery/jsp/icons/sound_133x100.png");
    expected.put(MEDIUM, "/silverpeas/gallery/jsp/icons/sound_266x150.png");
    expected.put(LARGE, "/silverpeas/gallery/jsp/icons/sound_266x150.png");
    expected.put(PREVIEW, "/silverpeas/gallery/jsp/icons/sound_266x150.png");
    expected.put(WATERMARK, "");
    expected.put(ORIGINAL, "/silverpeas/gallery/jsp/icons/sound_266x150.png");
    for (MediaResolution mediaResolution : MediaResolution.values()) {
      assertThat(sound.getApplicationThumbnailUrl(mediaResolution),
          is(expected.get(mediaResolution)));
    }
  }

  private Sound defaultSound() {
    Sound sound = new Sound();
    sound.setId("mediaId");
    sound.setComponentInstanceId("instanceId");
    sound.setBitrate(2048);
    sound.setDuration(72000000);
    sound.setCreationDate(TODAY);
    sound.setFileName("soundFile.mp3");
    assertDefaultSound(sound);
    return sound;
  }

  private void assertDefaultSound(Sound sound) {
    assertThat(sound.getType(), is(MediaType.Sound));
    assertThat(sound.getWorkspaceSubFolderName(), is("soundmediaId"));
    assertThat(sound.getBitrate(), is(2048L));
    assertThat(sound.getDuration(), is(72000000L));
    assertThat(sound.getApplicationOriginalUrl(),
        is(GALLERY_REST_WEB_SERVICE_BASE_URI + "sounds/mediaId/content?_t=1393628400000"));
    assertThat(FilenameUtils.normalize(sound.getFile(MediaResolution.ORIGINAL).getPath(), true),
        endsWith("/instanceId/soundmediaId/soundFile.mp3"));
  }
}