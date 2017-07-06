/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.components.gallery.constant;

import org.silverpeas.core.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: Yohann Chastagnier
 */
public enum StreamingProvider {
  unknown(null, null),
  youtube("(?i)(\\?|&)v=([a-z0-9]+)", "http://www.youtube.com/oembed?url={0}&format=json", "youtu"),
  vimeo("(?i)(/|=)([0-9]+)", "http://vimeo.com/api/oembed.json?url=http://vimeo.com/{0}"),
  dailymotion("(?i)(/video/)([a-z0-9]+)_",
      "http://www.dailymotion.com/services/oembed?url=http://www.dailymotion.com/video/{0}"),
  soundcloud("(?i)(soundcloud.com/)(.+)",
      "http://soundcloud.com/oembed?url=http://soundcloud.com/{0}&format=json");

  public static final Set<StreamingProvider> ALL_VALIDS;

  private final Pattern isExtractorPattern;
  private final String oembedUrlPattern;
  private final List<String> regexpDetectionParts;

  static {
    Set<StreamingProvider> allValids = EnumSet.allOf(StreamingProvider.class);
    allValids.remove(unknown);
    //noinspection unchecked
    ALL_VALIDS = Collections.unmodifiableSet(allValids);
  }

  StreamingProvider(final String idExtractorPattern, final String oembedUrlPattern,
      final String... regexpDetectionParts) {
    if (StringUtil.isDefined(idExtractorPattern)) {
      isExtractorPattern = Pattern.compile(idExtractorPattern);
    } else {
      isExtractorPattern = null;
    }
    this.oembedUrlPattern = oembedUrlPattern;
    this.regexpDetectionParts = new ArrayList<String>();
    this.regexpDetectionParts.add(name());
    Collections.addAll(this.regexpDetectionParts, regexpDetectionParts);
  }

  /**
   * Retrieves from the specified string the streaming provider.
   * @param provider the exact string of the provider (it is not case sensitive)
   * @return the streaming provider found if any, {@link #unknown} otherwise.
   */
  @JsonCreator
  public static StreamingProvider from(String provider) {
    try {
      return valueOf(provider.toLowerCase());
    } catch (Exception e) {
      return unknown;
    }
  }

  /**
   * Retrieves from the specified string the streaming provider.
   * @param streamingUrl the exact string of the provider (it is not case sensitive)
   * @return the streaming provider found if any, {@link #unknown} otherwise.
   */
  public static StreamingProvider fromUrl(String streamingUrl) {
    if (StringUtil.isDefined(streamingUrl)) {
      for (StreamingProvider streamingProvider : ALL_VALIDS) {
        for (String name : streamingProvider.getRegexpDetectionParts()) {
          if (Pattern.compile(name).matcher(streamingUrl.toLowerCase()).find()) {
            return streamingProvider;
          }
        }
      }
    }
    return unknown;
  }

  /**
   * Gets the oembed url from the homepage url of a streaming.<br/>
   * @param homepageUrl the streaming home page url.
   * @return the oembed url as string.
   */
  public static String getOembedUrl(String homepageUrl) {
    StreamingProvider streamingProvider = StreamingProvider.fromUrl(homepageUrl);
    if (streamingProvider != unknown) {
      final String streamingId;
      switch (streamingProvider) {
        case youtube:
          streamingId = homepageUrl;
          break;
        default:
          streamingId = streamingProvider.extractStreamingId(homepageUrl);
          break;
      }
      return MessageFormat.format(streamingProvider.oembedUrlPattern, streamingId);
    }
    return null;
  }

  @JsonValue
  public String getName() {
    return name();
  }

  /**
   * Gets the regexp detection parts
   * @return
   */
  public List<String> getRegexpDetectionParts() {
    return regexpDetectionParts;
  }

  /**
   * Indicates if the provider is an unknown one.
   * @return true if unknown, false otherwise.
   */
  public boolean isUnknown() {
    return this == unknown;
  }

  /**
   * Gets the identifier of the stream from an url.
   * @param url
   * @return
   */
  public String extractStreamingId(String url) {
    Matcher matcher = isExtractorPattern.matcher(url);
    if (matcher.find()) {
      return matcher.group(2);
    }
    return "";
  }
}
