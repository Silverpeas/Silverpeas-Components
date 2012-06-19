/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.rssAgregator.model;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import de.nava.informa.core.ImageIF;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.Item;
/**
 * Use RSSItem to encapsulate RSS news or ATOM data. It allows us having light JSON transfer data.
 * Users who want more news about RSS only need to click on the items link. 
 * @author ebonnet
 */
public class RSSItem implements Serializable, Comparable<RSSItem> {

  private static final long serialVersionUID = -1235557051682143463L;

  /**
   * Item attributes
   */
  private String itemTitle;
  private String itemDescription;
  private URL itemLink;
  private String itemSubject;
  private Date itemDate;
  private URL itemComments;

  /**
   * Channel attributes
   */
  private Long channelId;
  private String channelTitle;
  private String channelDescription;
  private ImageIF channelImage;

  /**
   * Default RSSItem constructor which encapsulate Item and Channel from informa API
   * @param item
   * @param channel
   */
  public RSSItem(Item item, Channel channel) {
    this.itemTitle = item.getTitle();
    this.itemDescription = item.getDescription();
    this.itemLink = item.getLink();
    this.itemSubject = item.getSubject();
    this.itemDate = item.getDate();
    this.itemComments = item.getComments();
    this.channelId = channel.getId();
    this.channelTitle = channel.getTitle();
    this.channelImage = channel.getImage();
    this.channelDescription = channel.getDescription();
  }

  /**
   * @return the itemTitle
   */
  public String getItemTitle() {
    return itemTitle;
  }

  /**
   * @param itemTitle the itemTitle to set
   */
  public void setItemTitle(String itemTitle) {
    this.itemTitle = itemTitle;
  }

  /**
   * @return the itemDescription
   */
  public String getItemDescription() {
    return itemDescription;
  }

  /**
   * @param itemDescription the itemDescription to set
   */
  public void setItemDescription(String itemDescription) {
    this.itemDescription = itemDescription;
  }

  /**
   * @return the itemURL
   */
  public URL getItemLink() {
    return itemLink;
  }

  /**
   * @param itemURL the itemURL to set
   */
  public void setItemLink(URL itemURL) {
    this.itemLink = itemURL;
  }

  /**
   * @return the itemSubject
   */
  public String getItemSubject() {
    return itemSubject;
  }

  /**
   * @param itemSubject the itemSubject to set
   */
  public void setItemSubject(String itemSubject) {
    this.itemSubject = itemSubject;
  }

  /**
   * @return the itemDate
   */
  public Date getItemDate() {
    return itemDate;
  }

  /**
   * @param itemDate the itemDate to set
   */
  public void setItemDate(Date itemDate) {
    this.itemDate = itemDate;
  }

  /**
   * @return the itemComments
   */
  public URL getItemComments() {
    return itemComments;
  }

  /**
   * @param itemComments the itemComments to set
   */
  public void setItemComments(URL itemComments) {
    this.itemComments = itemComments;
  }

  /**
   * @return the channelId
   */
  public Long getChannelId() {
    return channelId;
  }

  /**
   * @param channelId the channelId to set
   */
  public void setChannelId(Long channelId) {
    this.channelId = channelId;
  }

  /**
   * @return the channelTitle
   */
  public String getChannelTitle() {
    return channelTitle;
  }

  /**
   * @param channelTitle the channelTitle to set
   */
  public void setChannelTitle(String channelTitle) {
    this.channelTitle = channelTitle;
  }

  /**
   * @return the channelDescription
   */
  public String getChannelDescription() {
    return channelDescription;
  }

  /**
   * @param channelDescription the channelDescription to set
   */
  public void setChannelDescription(String channelDescription) {
    this.channelDescription = channelDescription;
  }

  /**
   * @return the image
   */
  public ImageIF getChannelImage() {
    return channelImage;
  }

  /**
   * @param image the image to set
   */
  public void setChannelImage(ImageIF image) {
    this.channelImage = image;
  }

  @Override
  public int compareTo(RSSItem other) {
    if (this.getItemDate() != null && other.getItemDate() != null) {
      return other.getItemDate().compareTo(this.getItemDate());
    } else if (this.getItemDate() == null && other.getItemDate() != null) {
      return 1;
    } else if (this.getItemDate() != null && other.getItemDate() == null) {
      return -1;
    }
    return 0;
  }
}
