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
import org.silverpeas.media.Definition;
import org.silverpeas.media.web.MediaDefinitionEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ebonnet
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OembedDataEntity {

  @XmlElement(name = "provider_name")
  private StreamingProvider provider;

  @XmlElement
  private String title;

  @XmlElement(name = "author_name")
  private String author;

  @XmlElement
  private String html;

  @XmlElement
  private String width;

  @XmlElement
  private String height;

  @XmlElement
  private String duration;

  @XmlElement
  private String version;

  /**
   * Constructor
   * @param streamingProvider the Silverpeas provider identifier.
   */
  protected OembedDataEntity(StreamingProvider streamingProvider) {
    this.provider = streamingProvider;
  }

  protected OembedDataEntity() {
  }

  public StreamingProvider getProvider() {
    return provider;
  }

  public void setProvider(final StreamingProvider provider) {
    this.provider = provider;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(final String author) {
    this.author = author;
  }

  public MediaDefinitionEntity getDefinition() {
    return MediaDefinitionEntity
        .createFrom(Definition.of(Integer.valueOf(getWidth()), Integer.valueOf(getHeight())));
  }

  public String getHtml() {
    return html;
  }

  public void setHtml(final String html) {
    this.html = html;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(final String width) {
    this.width = width;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(final String height) {
    this.height = height;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(final String duration) {
    this.duration = duration;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }
}
