/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
package com.silverpeas.gallery.model;

import com.silverpeas.gallery.control.ejb.MediaServiceFactory;
import com.silverpeas.util.StringUtil;

import java.util.Date;

public class OrderRow {
  private String orderId;
  private String mediaId;
  private InternalMedia internalMedia;
  private String instanceId;
  private Date downloadDate;
  private String downloadDecision;

  public OrderRow(String orderId, String mediaId, String instanceId) {
    setOrderId(orderId);
    setMediaId(mediaId);
    setInstanceId(instanceId);
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getMediaId() {
    return mediaId;
  }

  public void setMediaId(String mediaId) {
    if (StringUtil.isNotDefined(mediaId) || mediaId.equals(this.mediaId)) {
      internalMedia = null;
    }
    this.mediaId = mediaId;
  }

  public Date getDownloadDate() {
    return downloadDate;
  }

  public void setDownloadDate(Date downloadDate) {
    this.downloadDate = downloadDate;
  }

  public String getDownloadDecision() {
    return downloadDecision;
  }

  public void setDownloadDecision(String downloadDecision) {
    this.downloadDecision = downloadDecision;
  }

  public InternalMedia getInternalMedia() {
    if (internalMedia == null) {
      internalMedia = (InternalMedia) MediaServiceFactory.getMediaService()
          .getMedia(new MediaPK(getMediaId(), getInstanceId()));
    }
    return internalMedia;
  }
}
