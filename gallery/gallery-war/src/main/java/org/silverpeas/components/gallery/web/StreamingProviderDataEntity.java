/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.gallery.web;

import org.silverpeas.components.gallery.constant.StreamingProvider;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.components.gallery.model.Streaming;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.webapi.media.MediaDefinitionEntity;
import org.silverpeas.core.util.JSONCodec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * This entity ensures that all streaming data are formatted in a single way whatever the
 * streaming provider.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamingProviderDataEntity implements WebEntity {
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

  @XmlElement
  private URI thumbnailUrl;

  @XmlElement
  private MediaDefinitionEntity thumbnailDefinition;

  @XmlElement
  private List<URI> thumbnailPreviewUrls = new ArrayList<URI>();

  /**
   * Creates a streaming provider data entity from specified homepage url.
   * @param homepageUrl the streaming home page url.
   * @return the streaming provider data entity representing the specified streaming.
   */
  public static StreamingProviderDataEntity from(final String homepageUrl) {
    StreamingProviderDataEntity entity = null;
    final StreamingProvider streamingProvider = StreamingProvider.fromUrl(homepageUrl);
    if (streamingProvider != StreamingProvider.unknown) {
      final OembedDataEntity oembedData =
          JSONCodec.decode(Streaming.getJsonOembedAsString(homepageUrl), OembedDataEntity.class);
      switch (streamingProvider) {
        case youtube:
          entity = new YoutubeDataEntity(oembedData);
          break;
        case vimeo:
          entity = new VimeoDataEntity(oembedData);
          break;
        case dailymotion:
          entity = new DailymotionDataEntity(oembedData);
          break;
        case soundcloud:
          entity = new SoundcloudDataEntity(oembedData);
          break;
      }
    }
    return entity;
  }

  public StreamingProviderDataEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Constructor from OembedDataEntity ({@literal http://oembed.com}).
   * @param streamingProvider the Silverpeas provider identifier.
   * @param oembedData the oembed data as JSON format.
   */
  protected StreamingProviderDataEntity(StreamingProvider streamingProvider,
      final OembedDataEntity oembedData) {
    this.provider = streamingProvider;
    this.title = oembedData.getTitle();
    this.author = oembedData.getAuthor();
    this.embedHtml = oembedData.getHtml();

    String thumbnailUrl = oembedData.getThumbnailUrl();
    if (isDefined(thumbnailUrl)) {
      this.thumbnailUrl = URI.create(thumbnailUrl);
      String thumbnailWidth = oembedData.getThumbnailWidth();
      String thumbnailHeight = oembedData.getThumbnailHeight();
      if (isDefined(thumbnailWidth) && isDefined(thumbnailHeight)) {
        this.thumbnailDefinition = MediaDefinitionEntity.createFrom(Definition
            .of(Integer.valueOf(thumbnailWidth.replaceAll("[^0-9].", "")),
                Integer.valueOf(thumbnailHeight.replaceAll("[^0-9].", ""))));
      }
    }
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

  public URI getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(final URI thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public MediaDefinitionEntity getThumbnailDefinition() {
    return thumbnailDefinition;
  }

  public void setThumbnailDefinition(final MediaDefinitionEntity thumbnailDefinition) {
    this.thumbnailDefinition = thumbnailDefinition;
  }

  public List<URI> getThumbnailPreviewUrls() {
    return thumbnailPreviewUrls;
  }

  public void setThumbnailPreviewUrls(final List<URI> thumbnailPreviewUrls) {
    this.thumbnailPreviewUrls = thumbnailPreviewUrls;
  }
}
