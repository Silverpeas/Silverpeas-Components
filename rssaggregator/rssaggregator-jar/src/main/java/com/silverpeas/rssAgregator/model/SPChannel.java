/*
 * Created on 20 août 2004
 *
 */
package com.silverpeas.rssAgregator.model;

import java.io.Serializable;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

import de.nava.informa.impl.basic.Channel;

/**
 * @author neysseri
 * 
 */
public class SPChannel extends SilverpeasBean implements Serializable {
  private String instanceId;
  private String url;
  private int nbDisplayedItems = 100;
  private int refreshRate;
  private int displayImage = 0;
  private String creatorId;
  private String creationDate;
  private Channel channel;

  public SPChannel() {
    super();
  }

  public SPChannel(String url) {
    setPK(new SPChannelPK("undefined"));
    setUrl(url);
  }

  public SPChannel(String id, String url) {
    setPK(new SPChannelPK(id));
    setUrl(url);
  }

  public SPChannel(String id, String url, String instanceId) {
    setPK(new SPChannelPK(id));
    setInstanceId(instanceId);
    setUrl(url);
  }

  public String getUrl() {
    return url;
  }

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
   * @return
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * @return
   */
  public int getNbDisplayedItems() {
    return nbDisplayedItems;
  }

  /**
   * @param string
   */
  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @param string
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * @param i
   */
  public void setNbDisplayedItems(int i) {
    this.nbDisplayedItems = i;
  }

  /**
   * @return
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param string
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(Object obj) {
    if (!(obj instanceof SPChannel))
      return 0;
    return (String.valueOf(getPK().getId())).compareTo(String
        .valueOf(((SPChannel) obj).getPK().getId()));
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

}