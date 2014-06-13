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

import com.silverpeas.gallery.constant.MediaType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class VideoTest {

  @Test
  public void justInstancedTest() {
    Video video = new Video();
    assertThat(video.getType(), is(MediaType.Video));
    assertThat(video.getResolutionW(), is(0));
    assertThat(video.getResolutionH(), is(0));
    assertThat(video.getBitrate(), is(0L));
    assertThat(video.getDuration(), is(0L));
  }

  @Test
  public void justCreatedTest() {
    Video video = defaultVideo();
    assertDefaultVideo(video);
  }

  private Video defaultVideo() {
    Video video = new Video();
    video.setResolutionW(800);
    video.setResolutionH(600);
    video.setBitrate(1024);
    video.setDuration(36000000);
    assertDefaultVideo(video);
    return video;
  }

  private void assertDefaultVideo(Video video) {
    assertThat(video.getType(), is(MediaType.Video));
    assertThat(video.getResolutionW(), is(800));
    assertThat(video.getResolutionH(), is(600));
    assertThat(video.getBitrate(), is(1024L));
    assertThat(video.getDuration(), is(36000000L));
  }
}