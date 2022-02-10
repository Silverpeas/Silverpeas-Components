/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.time.Duration;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;

public class NewsEntity implements WebEntity {

  private static final long serialVersionUID = 1L;
  
  @XmlElement(defaultValue = "")
  private URI uri;
  
  @XmlElement
  private String id;
  
  @XmlElement
  private String publicationId;

  @XmlElement
  private String componentId;

  @XmlElement
  private String title;

  @XmlElement
  private String description;
  
  @XmlElement
  private Date date;
  
  @XmlElement
  private String content;
  
  @XmlElement
  private String thumbnailURL;

  @XmlElement
  private String permalink;

  @XmlElement
  private Integer numberOfAttachments;
  
  public static NewsEntity fromNews(News news) {
    NewsEntity entity = new NewsEntity();
    entity.setTitle(news.getTitle());
    entity.setDescription(news.getDescription());
    entity.setDate(news.getOnlineDate());
    entity.setPermalink(news.getPermalink());
    entity.setId(news.getId());
    entity.setPublicationId(news.getPublicationId());
    entity.setComponentId(news.getComponentInstanceId());
    entity.setContent(news.getContent());
    return entity;
  }
  
  protected NewsEntity() {
  }

  void setExtraInfo(News news) {
    ThumbnailDetail thumbnail = news.getThumbnail();
    if (thumbnail != null) {
      setThumbnailURL(thumbnail.getURL());
    }
    setNumberOfAttachments(news.getNumberOfAttachments());
  }
  
  public String getTitle() {
    return title;
  }

  public void setTitle(String name) {
    this.title = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getPermalink() {
    return permalink;
  }

  public void setPermalink(String url) {
    this.permalink = url;
  }

  @Override
  public URI getURI() {
    return uri;
  }
  
  public NewsEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setPublicationId(String publicationId) {
    this.publicationId = publicationId;
  }

  public String getPublicationId() {
    return publicationId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }
  
  @XmlElement
  public String getPublishedFor() {
    Duration duration = UnitUtil.getDuration(DateUtil.getNow().getTime() - getDate().getTime());
    TimeUnit bestUnit = duration.getBestUnit();
    return UnitUtil.getDuration(new BigDecimal(String.valueOf(duration.getBestValue().intValue())),
        bestUnit).getBestDisplayValue();
  }
  
  @XmlElement
  public int getPublishedForNbDays() {
    return UnitUtil.getDuration(DateUtil.getNow().getTime() - getDate().getTime())
        .getTimeConverted(TimeUnit.DAY).intValue();
  }
  
  @XmlElement
  public String getPublishTime() {
    return DateUtil.formatTime(getDate());
  }
  
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setThumbnailURL(String thumbnailURL) {
    this.thumbnailURL = thumbnailURL;
  }

  public String getThumbnailURL() {
    return thumbnailURL;
  }

  public void setNumberOfAttachments(int numberOfAttachments) {
    this.numberOfAttachments = numberOfAttachments;
  }

  public Integer getNumberOfAttachments() {
    return numberOfAttachments;
  }

}
