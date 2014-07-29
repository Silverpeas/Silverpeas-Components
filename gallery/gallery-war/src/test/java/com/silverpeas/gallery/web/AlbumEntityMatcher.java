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

import com.silverpeas.gallery.model.AlbumDetail;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.silverpeas.gallery.web.GalleryResourceMock.*;

/**
 * @author Yohann Chastagnier
 */
public class AlbumEntityMatcher extends BaseMatcher<LinkedHashMap<String, Object>> {

  private final AlbumDetail expected;

  protected AlbumEntityMatcher(final AlbumDetail expected) {
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
      final AlbumEntity actual = from((LinkedHashMap<String, Object>) item);
      final EqualsBuilder matcher = new EqualsBuilder();
      matcher.appendSuper(actual.getURI().toString()
                                .endsWith("/gallery/componentName5/albums/3"));
      matcher.appendSuper(
          actual.getParentURI().toString().endsWith("/gallery/componentName5/albums/0"));
      matcher.append(String.valueOf(expected.getId()), actual.getId());
      matcher.append(expected.getName(), actual.getTitle());
      matcher.append(expected.getDescription(), actual.getDescription());
      matcher.append(4, actual.getMediaList().size());
      Set<String> keySet = actual.getMediaList().keySet();
      matcher.appendSuper(keySet.contains(PHOTO_ID));
      matcher.appendSuper(keySet.contains(VIDEO_ID));
      matcher.appendSuper(keySet.contains(SOUND_ID));
      matcher.appendSuper(keySet.contains(STREAMING_ID));
      match = matcher.isEquals();
    }
    return match;
  }

  public static AlbumEntityMatcher matches(final AlbumDetail expected) {
    return new AlbumEntityMatcher(expected);
  }

  @SuppressWarnings("unchecked")
  public static AlbumEntity from(LinkedHashMap<String, Object> album) {
    AlbumEntity albumEntity = new AlbumEntity();
    albumEntity.withURI(URI.create((String) album.get("uri")));
    albumEntity.withParentURI(URI.create((String) album.get("parentURI")));
    albumEntity.setId((String) album.get("id"));
    albumEntity.setTitle((String) album.get("title"));
    albumEntity.setDescription((String) album.get("description"));
    LinkedHashMap<String, LinkedHashMap<String, Object>> mediaList =
        (LinkedHashMap<String, LinkedHashMap<String, Object>>) album.get("mediaList");
    for (Map.Entry<String, LinkedHashMap<String, Object>> mediaEntry : mediaList.entrySet()) {
      albumEntity.addMedia(MediaEntityMatcher.from(mediaEntry.getValue()));
    }
    return albumEntity;
  }
}
