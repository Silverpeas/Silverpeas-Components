/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.components.gallery.GalleryWarBuilder;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.constant.StreamingProvider;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.io.file.SilverpeasFile;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class StreamingIT extends AbstractMediaIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(StreamingIT.class).build();
  }

  @Test
  public void justInstancedTest() {
    Streaming streaming = new Streaming();
    assertThat(streaming.getType(), is(MediaType.Streaming));
    assertThat(streaming.getHomepageUrl(), is(""));
    assertThat(streaming.getProvider(), Matchers.is(StreamingProvider.unknown));
  }

  @Test
  public void justCreatedTest() {
    Streaming streaming = defaultStreaming();
    assertDefaultStreaming(streaming);

    Map<MediaResolution, String> expected = new HashMap<>();
    expected.put(MediaResolution.TINY, "/silverpeas/gallery/jsp/icons/streaming_66x50.png");
    expected.put(MediaResolution.SMALL, "/silverpeas/gallery/jsp/icons/streaming_133x100.png");
    expected.put(MediaResolution.MEDIUM, "/silverpeas/gallery/jsp/icons/streaming_266x150.png");
    expected.put(MediaResolution.LARGE, "/silverpeas/gallery/jsp/icons/streaming_266x150.png");
    expected.put(MediaResolution.PREVIEW, "/silverpeas/gallery/jsp/icons/streaming_266x150.png");
    expected.put(MediaResolution.WATERMARK, "");
    expected.put(MediaResolution.NORMAL, "/silverpeas/gallery/jsp/icons/streaming_266x150.png");
    expected.put(MediaResolution.ORIGINAL, "/silverpeas/gallery/jsp/icons/streaming_266x150.png");
    for (MediaResolution mediaResolution : MediaResolution.values()) {
      assertThat(streaming.getApplicationThumbnailUrl(mediaResolution),
          is(expected.get(mediaResolution)));
    }
  }

  private Streaming defaultStreaming() {
    Streaming streaming = new Streaming();
    streaming.setId("mediaId");
    streaming.setHomepageUrl("anUrl");
    streaming.setProvider(StreamingProvider.youtube);
    assertDefaultStreaming(streaming);
    return streaming;
  }

  private void assertDefaultStreaming(Streaming streaming) {
    assertThat(streaming.getType(), is(MediaType.Streaming));
    assertThat(streaming.getWorkspaceSubFolderName(), is("streamingmediaId"));
    assertThat(streaming.getHomepageUrl(), is("anUrl"));
    assertThat(streaming.getProvider(), is(StreamingProvider.youtube));
    assertThat(streaming.getApplicationOriginalUrl(),
        is(streaming.getApplicationThumbnailUrl(MediaResolution.PREVIEW)));
    assertThat(streaming.getFile(MediaResolution.ORIGINAL), is(SilverpeasFile.NO_FILE));
  }
}