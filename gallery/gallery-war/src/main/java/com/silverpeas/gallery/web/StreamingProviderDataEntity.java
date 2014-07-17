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
package com.silverpeas.gallery.web;

import com.silverpeas.gallery.constant.StreamingProvider;
import com.silverpeas.web.Exposable;
import org.json.JSONObject;
import org.silverpeas.media.Definition;
import org.silverpeas.media.web.MediaDefinitionEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * This entity ensures that all streaming data are formatted in a single way whatever the
 * streaming provider.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class StreamingProviderDataEntity implements Exposable {
  private static final long serialVersionUID = 4017230238128160967L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement
  private StreamingProvider provider;

  @XmlElement
  private String title;

  @XmlElement
  private String author;

  @XmlElement
  private String formattedDurationHMS;

  @XmlElement
  private MediaDefinitionEntity definition =
      MediaDefinitionEntity.createFrom(Definition.fromZero());

  @XmlElement
  private String embedHtml;

  public StreamingProviderDataEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Constructor from oembed data ({@literal http://oembed.com}).
   * @param streamingProvider the Silverpeas provider identifier.
   * @param oembedData the oembed data as JSON format.
   */
  protected StreamingProviderDataEntity(StreamingProvider streamingProvider,
      final JSONObject oembedData) {
    this.provider = streamingProvider;
    this.title = oembedData.getString("title");
    this.author = oembedData.getString("author_name");
    this.embedHtml = oembedData.getString("html");
  }

  @SuppressWarnings("UnusedDeclaration")
  protected StreamingProviderDataEntity() {
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public void setUri(final URI uri) {
    this.uri = uri;
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

  public String getFormattedDurationHMS() {
    return formattedDurationHMS;
  }

  public void setFormattedDurationHMS(final String formattedDurationHMS) {
    this.formattedDurationHMS = formattedDurationHMS;
  }

  public MediaDefinitionEntity getDefinition() {
    return definition;
  }

  public void setDefinition(final MediaDefinitionEntity definition) {
    this.definition = definition;
  }

  public String getEmbedHtml() {
    return embedHtml;
  }

  public void setEmbedHtml(final String embedHtml) {
    this.embedHtml = embedHtml;
  }
}
