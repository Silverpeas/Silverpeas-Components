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

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

import de.nava.informa.impl.basic.Channel;

/**
 * @author neysseri
 */
public class SPChannel extends SilverpeasBean implements Serializable {

  private static final long serialVersionUID = 2610576839907057656L;
  private String instanceId;
  private String url;
  private int nbDisplayedItems = 100;
  private int refreshRate;
  private int displayImage = 0;
  private String creatorId;
  private String creationDate;
  private Channel channel;

  /**
   * Default constructor
   */
  public SPChannel() {
    super();
  }

  public SPChannel(String url) {
    setPK(new SPChannelPK("undefined"));
    this.url = url;
  }

  public SPChannel(String id, String url) {
    setPK(new SPChannelPK(id));
    this.url = url;
  }

  /**
   * Constructor with parameters
   * @param id the channel identifier
   * @param url the RSS url
   * @param instanceId the instance identifier
   */
  public SPChannel(String id, String url, String instanceId) {
    this(id, url);
    this.instanceId = instanceId;
  }

  /**
   * @return String representation URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set url
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return
   */
  public String getCreationDate() {
    return creationDate;
  }

  /**
   * @return the creator identifier
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * @return number of displayed items
   */
  public int getNbDisplayedItems() {
    return nbDisplayedItems;
  }

  /**
   * @param creationDate the creation date
   */
  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @param creatorId the creator identifier
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * @param nbDisplayedItems
   */
  public void setNbDisplayedItems(int nbDisplayedItems) {
    this.nbDisplayedItems = nbDisplayedItems;
  }

  /**
   * @return the instance identifier
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId the instance identifier to set
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(Object obj) {
    if (!(obj instanceof SPChannel)) {
      return 0;
    }
    return (String.valueOf(getPK().getId())).compareTo(String.valueOf(((SPChannel) obj).getPK()
        .getId()));
  }

  public String _getTableName() {
    return "SC_Rss_Channels";
  }

  /**
   * @return
   */
  public int getRefreshRate() {
    return refreshRate;
  }

  /**
   * @param i
   */
  public void setRefreshRate(int i) {
    refreshRate = i;
  }

  /**
   * @return
   */
  public Channel _getChannel() {
    return channel;
  }

  /**
   * @param channel
   */
  public void _setChannel(Channel channel) {
    this.channel = channel;
  }

  /**
   * @return
   */
  public int getDisplayImage() {
    return displayImage;
  }

  /**
   * @param i
   */
  public void setDisplayImage(int i) {
    displayImage = i;
  }

  /**
   * @return the channel
   */
  public Channel getChannel() {
    return channel;
  }

  /**
   * @param channel the channel to set
   */
  public void setChannel(Channel channel) {
    this.channel = channel;
  }

}