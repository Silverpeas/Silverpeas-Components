/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.web;

import com.silverpeas.gallery.constant.StreamingProvider;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.UnitUtil;
import org.silverpeas.util.time.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class VimeoDataEntity extends StreamingProviderDataEntity {
  private static final long serialVersionUID = 724049725696379973L;

  /**
   * Creates a vimeo provider data entity from specified OEmbed data.
   * @param oembedVimeoData the oembed data entity ({@literal http://oembed.com}) construct from
   * JSON format.
   * @return the entity representing the specified streaming.
   */
  public static VimeoDataEntity fromOembed(final OembedDataEntity oembedVimeoData) {
    return new VimeoDataEntity(oembedVimeoData);
  }

  /**
   * Default hidden constructor.
   */
  private VimeoDataEntity(final OembedDataEntity oembedVimeoData) {
    super(StreamingProvider.vimeo, oembedVimeoData);

    // As a specific way, vimeo is supplying additional information about the video streaming
    // duration.
    if (StringUtil.isInteger(oembedVimeoData.getDuration())) {
      setFormattedDurationHMS(
          UnitUtil.getTimeData(Long.valueOf(oembedVimeoData.getDuration()), TimeUnit.SEC)
              .getFormattedDurationAsHMS());
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  protected VimeoDataEntity() {
    super();
  }
}
