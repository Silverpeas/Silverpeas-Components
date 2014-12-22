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
import org.silverpeas.media.Definition;

import java.util.HashMap;
import java.util.Map;

import static com.silverpeas.gallery.constant.MediaResolution.*;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class VideoTest extends AbstractMediaTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(VideoTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addClasses(GalleryComponentSettings.class);
          warBuilder.addPackages(true, "com.silverpeas.gallery.constant");
          warBuilder.addPackages(true, "com.silverpeas.gallery.model");
          warBuilder.addAsResource("maven.properties");
        }).build();
  }

  @Test
  public void justInstancedTest() {
    Video video = new Video();
    assertThat(video.getType(), is(MediaType.Video));
    assertThat(video.getDefinition(), is(Definition.NULL));
    assertThat(video.getDefinition().getWidth(), is(0));
    assertThat(video.getDefinition().getHeight(), is(0));
    assertThat(video.getBitrate(), is(0L));
    assertThat(video.getDuration(), is(0L));
  }

  @Test
  public void justCreatedTest() {
    Video video = defaultVideo();
    assertDefaultVideo(video);

    Map<MediaResolution, String> expected = new HashMap<>();
    expected.put(TINY, "/silverpeas/gallery/jsp/icons/video_66x50.png");
    expected.put(SMALL, "/silverpeas/gallery/jsp/icons/video_133x100.png");
    expected.put(MEDIUM, "/silverpeas/gallery/jsp/icons/video_266x150.png");
    expected.put(LARGE, "/silverpeas/gallery/jsp/icons/video_266x150.png");
    expected.put(PREVIEW, "/silverpeas/gallery/jsp/icons/video_266x150.png");
    expected.put(WATERMARK, "");
    expected.put(ORIGINAL, "/silverpeas/gallery/jsp/icons/video_266x150.png");
    for (MediaResolution mediaResolution : MediaResolution.values()) {
      assertThat(video.getApplicationThumbnailUrl(mediaResolution),
          is(expected.get(mediaResolution)));
    }
  }

  private Video defaultVideo() {
    Video video = new Video();
    video.setId("mediaId");
    video.setComponentInstanceId("instanceId");
    video.setDefinition(Definition.of(800, 600));
    video.setBitrate(1024);
    video.setDuration(36000000);
    video.setCreationDate(TODAY);
    video.setFileName("videoFile.mp4");
    assertDefaultVideo(video);
    return video;
  }

  private void assertDefaultVideo(Video video) {
    assertThat(video.getType(), is(MediaType.Video));
    assertThat(video.getWorkspaceSubFolderName(), is("videomediaId"));
    assertThat(video.getDefinition().getWidth(), is(800));
    assertThat(video.getDefinition().getHeight(), is(600));
    assertThat(video.getBitrate(), is(1024L));
    assertThat(video.getDuration(), is(36000000L));
    assertThat(video.getApplicationOriginalUrl(),
        is(GALLERY_REST_WEB_SERVICE_BASE_URI + "videos/mediaId/content?_t=1393628400000"));
    assertThat(FilenameUtils.normalize(video.getFile(MediaResolution.ORIGINAL).getPath(), true),
        endsWith("/instanceId/videomediaId/videoFile.mp4"));
  }
}