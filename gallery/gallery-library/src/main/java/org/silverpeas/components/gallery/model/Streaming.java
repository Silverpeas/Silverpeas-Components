/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.model;

import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.media.streaming.StreamingProvider;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * This class represents a Streaming.
 */
public class Streaming extends Media {
  private static final long serialVersionUID = 5772513957256327862L;

  private String homepageUrl = "";
  private StreamingProvider provider;

  @Override
  public MediaType getType() {
    return MediaType.Streaming;
  }

  public Streaming() {
    super();
  }

  protected Streaming(final Streaming other) {
    super(other);
    this.homepageUrl = other.homepageUrl;
    this.provider = other.provider;
  }

  /**
   * Gets the provider URL that permits to access to the video play.
   * @return the provider URL that permits to access to the video play.
   */
  public String getHomepageUrl() {
    return homepageUrl;
  }

  /**
   * Sets the provider URL that permits to access to the video play.
   * @param url the provider URL that permits to access to the video play.
   */
  public void setHomepageUrl(final String url) {
    this.homepageUrl = url;
  }

  /**
   * Gets the streaming provider.
   * @return the streaming provider.
   */
  public Optional<StreamingProvider> getProvider() {
    return ofNullable(provider);
  }

  /**
   * Gets the streaming provider.
   * @param provider the streaming provider.
   */
  public void setProvider(final StreamingProvider provider) {
    this.provider = provider;
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return MediaType.Streaming.name();
  }

  @Override
  public String getApplicationOriginalUrl() {
    if (StringUtil.isNotDefined(getId())) {
      return super.getApplicationOriginalUrl();
    }
    return getApplicationThumbnailUrl(MediaResolution.PREVIEW);
  }

  @Override
  public SilverpeasFile getFile(final MediaResolution mediaResolution, final String size) {
    return SilverpeasFile.NO_FILE;
  }

  @Override
  public String getApplicationEmbedUrl(final MediaResolution mediaResolution) {
    return "";
  }

  @Override
  public Streaming getCopy() {
    return new Streaming(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final Streaming streaming = (Streaming) o;
    return Objects.equals(homepageUrl, streaming.homepageUrl) &&
        Objects.equals(provider, streaming.provider);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), homepageUrl, provider);
  }
}
