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

import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.model.Media;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.net.URI;
import java.util.LinkedHashMap;

/**
 * @author Yohann Chastagnier
 */
public class MediaEntityMatcher extends BaseMatcher<LinkedHashMap<String, Object>> {

  private final Media expected;

  protected MediaEntityMatcher(final Media expected) {
    this.expected = expected;
  }

  @Override
  public void describeTo(final Description description) {
    description.appendValue(expected);
  }

  /*
   * (non-Javadoc)
   * @see org.hamcrest.Matcher#matches(java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean matches(final Object item) {
    boolean match = false;
    if (item instanceof LinkedHashMap) {
      final AbstractMediaEntity actual = from((LinkedHashMap<String, Object>) item);
      final EqualsBuilder matcher = new EqualsBuilder();
      matcher.appendSuper(actual.getURI().toString().endsWith(
          "/gallery/componentName5/albums/3/" + expected.getType().getMediaWebUriPart() + "/" +
              expected.getId()));
      matcher.appendSuper(
          actual.getParentURI().toString().endsWith("/gallery/componentName5/albums/3"));
      matcher.append(expected.getType(), actual.getType());
      matcher.append(expected.getId(), actual.getId());
      matcher.append(expected.getTitle(), actual.getTitle());
      matcher.append(expected.getDescription(), actual.getDescription());
      // URL
      if (expected.getInternalMedia() != null) {
        matcher.appendSuper(actual.getUrl().toString()
            .endsWith("/gallery/componentName5/" + expected.getType().getMediaWebUriPart() + "/" +
                expected.getId() + "/content?_t=1325372400000"));
      } else {
        matcher.appendSuper(
            actual.getUrl().toString().equals("/homepageUrl/componentName5/" + expected.getId()));
      }
      // Other URLs
      if (expected.getType().isPhoto()) {
        PhotoEntity photoEntity = (PhotoEntity) actual;
        matcher.appendSuper(photoEntity.getPreviewUrl().toString().contains(
            "/silverpeas/services/gallery/componentName5/photos/7/content?_t=1325372400000" +
                "&resolution=PREVIEW"));
        matcher.appendSuper(photoEntity.getThumbUrl().toString()
            .contains("/silverpeas/gallery/jsp/icons/notAvailable_fr_133x100.jpg"));
      } else {
        matcher.appendSuper(actual.getThumbUrl().toString()
            .equals("/silverpeas/gallery/jsp/icons/" + expected.getType().getName().toLowerCase() +
                "_266x150.png"));
      }
      match = matcher.isEquals();
    }
    return match;
  }

  public static MediaEntityMatcher matches(final Media expected) {
    return new MediaEntityMatcher(expected);
  }

  @SuppressWarnings("unchecked")
  public static AbstractMediaEntity from(LinkedHashMap<String, Object> media) {
    MediaType mediaType = MediaType.from((String) media.get("type"));
    AbstractMediaEntity mediaEntity = null;
    switch (mediaType) {
      case Unknown:
        throw new IllegalArgumentException();
      case Photo:
        mediaEntity = new PhotoEntity();
        ((PhotoEntity) mediaEntity).withPreviewUrl(URI.create((String) media.get("previewUrl")));
        break;
      case Video:
        mediaEntity = new VideoEntity();
        break;
      case Sound:
        mediaEntity = new SoundEntity();
        break;
      case Streaming:
        mediaEntity = new StreamingEntity();
        break;
    }
    mediaEntity.setType(mediaType);
    mediaEntity.withURI(URI.create((String) media.get("uri")));
    mediaEntity.withParentURI(URI.create((String) media.get("parentURI")));
    mediaEntity.withOriginalUrl(URI.create((String) media.get("url")));
    mediaEntity.withThumbUrl(URI.create((String) media.get("thumbUrl")));
    mediaEntity.setId((String) media.get("id"));
    mediaEntity.setTitle((String) media.get("title"));
    mediaEntity.setDescription((String) media.get("description"));
    return mediaEntity;
  }
}
