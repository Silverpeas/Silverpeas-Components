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

import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.model.Sound;
import com.silverpeas.gallery.model.Streaming;
import com.silverpeas.gallery.model.Video;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.file.SilverpeasFile;

import java.sql.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public class MediaBuilder {

  public static MediaBuilder getMediaBuilder() {
    return new MediaBuilder();
  }

  public Photo buildPhoto(final String photoId, final String componentId) {
    return new PhotoMock(photoId, componentId);
  }

  public Video buildVideo(final String videoId, final String componentId) {
    return new VideoMock(videoId, componentId);
  }

  public Sound buildSound(final String soundId, final String componentId) {
    return new SoundMock(soundId, componentId);
  }

  public Streaming buildStreaming(final String streamingId, final String componentId) {
    return new StreamingMock(streamingId, componentId);
  }

  private MediaBuilder() {
    // Nothing to do
  }

  protected static void fillCommonData(Media media, String mediaId, String componentId) {
    media.setTitle("title" + mediaId);
    media.setDescription("description" + mediaId);
    media.setCreationDate(Date.valueOf("2012-01-01"));
    media.setLastUpdateDate(Date.valueOf("2012-01-01"));
    media.setAuthor("author");
    media.setMediaPK(new MediaPK(mediaId, componentId));
  }

  protected static void fillInternalData(InternalMedia media) {
    media.setDownloadAuthorized(true);
    media.setFileName("imageName" + media.getId());
  }

  /**
   * Mock of photo entity
   */
  protected class PhotoMock extends Photo {
    private static final long serialVersionUID = 4732115283664783337L;

    public PhotoMock(final String photoId, final String componentId) {
      fillCommonData(this, photoId, componentId);
      fillInternalData(this);
    }

    @Override
    public boolean canBeAccessedBy(final UserDetail user) {
      return true;
    }

    @Override
    public SilverpeasFile getFile(final MediaResolution mediaResolution) {
      SilverpeasFile mock = mock(SilverpeasFile.class);
      when(mock.exists()).thenReturn(true);
      return mock;
    }
  }

  /**
   * Mock of video entity
   */
  protected class VideoMock extends Video {
    private static final long serialVersionUID = 5629495536517838355L;

    public VideoMock(final String videoId, final String componentId) {
      fillCommonData(this, videoId, componentId);
      fillInternalData(this);
    }

    @Override
    public boolean canBeAccessedBy(final UserDetail user) {
      return true;
    }

    @Override
    public SilverpeasFile getFile(final MediaResolution mediaResolution) {
      SilverpeasFile mock = mock(SilverpeasFile.class);
      when(mock.exists()).thenReturn(true);
      return mock;
    }
  }

  /**
   * Mock of sound entity
   */
  protected class SoundMock extends Sound {
    private static final long serialVersionUID = 5404581049606305308L;

    public SoundMock(final String soundId, final String componentId) {
      fillCommonData(this, soundId, componentId);
      fillInternalData(this);
    }

    @Override
    public boolean canBeAccessedBy(final UserDetail user) {
      return true;
    }

    @Override
    public SilverpeasFile getFile(final MediaResolution mediaResolution) {
      SilverpeasFile mock = mock(SilverpeasFile.class);
      when(mock.exists()).thenReturn(true);
      return mock;
    }
  }

  /**
   * Mock of streaming entity
   */
  protected class StreamingMock extends Streaming {
    private static final long serialVersionUID = -3883116179770827128L;

    public StreamingMock(final String streamingId, final String componentId) {
      fillCommonData(this, streamingId, componentId);
      setHomepageUrl("/homepageUrl/" + componentId + "/" + streamingId);
    }

    @Override
    public boolean canBeAccessedBy(final UserDetail user) {
      return true;
    }
  }
}
