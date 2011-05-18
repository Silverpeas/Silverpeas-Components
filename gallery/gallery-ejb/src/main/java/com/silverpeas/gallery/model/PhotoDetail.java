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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.gallery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileServerUtils;

public class PhotoDetail implements SilverContentInterface, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  PhotoPK photoPK;
  String title;
  String description;
  int sizeH;
  int sizeL;
  Date creationDate;
  Date updateDate;
  String vueDate;
  String author;
  boolean download = false;
  boolean albumLabel = false;
  String status;
  String albumId;
  String creatorId;
  String creatorName;
  String updateId;
  String updateName;
  String imageName;
  long imageSize;
  String imageMimeType;
  Date beginDate;
  Date endDate;
  String permalink;
  LinkedHashMap<String, MetaData> metaData = new LinkedHashMap<String, MetaData>();
  String keyWord;
  Date beginDownloadDate;
  Date endDownloadDate;

  private String silverObjectId; // added for the components - PDC integration
  private String iconUrl;

  public String getKeyWord() {
    return keyWord;
  }

  public void setKeyWord(String keyWord) {
    this.keyWord = keyWord;
  }

  public void setSilverObjectId(String silverObjectId) {
    this.silverObjectId = silverObjectId;
  }

  public void setSilverObjectId(int silverObjectId) {
    this.silverObjectId = new Integer(silverObjectId).toString();
  }

  public String getSilverObjectId() {
    return this.silverObjectId;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  public String getIconUrl() {
    return this.iconUrl;
  }

  public Date getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(Date beginDate) {
    this.beginDate = beginDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public PhotoDetail() {
  }

  public PhotoDetail(String title, String description, Date creationDate,
      Date updateDate, String vueDate, String author, boolean download,
      boolean albumLabel) {
    setTitle(title);
    setDescription(description);
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    setVueDate(vueDate);
    setAuthor(author);
    setDownload(download);
    setAlbumLabel(albumLabel);
  }

  public PhotoDetail(String title, String description, Date creationDate,
      Date updateDate, String vueDate, String author, boolean download,
      boolean albumLabel, Date beginDate, Date endDate, String keyWord,
      Date beginDownloadDate, Date endDownloadDate) {
    setTitle(title);
    setDescription(description);
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    setVueDate(vueDate);
    setAuthor(author);
    setDownload(download);
    setAlbumLabel(albumLabel);
    setBeginDate(beginDate);
    setEndDate(endDate);
    setKeyWord(keyWord);
    setBeginDownloadDate(beginDownloadDate);
    setEndDownloadDate(endDownloadDate);
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  /*
   * public String getAlbumId() { return albumId; }
   */

  public void setAlbumId(String albumId) {
    this.albumId = albumId;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public String getVueDate() {
    return vueDate;
  }

  public void setVueDate(String vueDate) {
    this.vueDate = vueDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getInstanceId() {
    return getPhotoPK().getInstanceId();
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getSizeH() {
    return sizeH;
  }

  public void setSizeH(int sizeH) {
    this.sizeH = sizeH;
  }

  public int getSizeL() {
    return sizeL;
  }

  public void setSizeL(int sizeL) {
    this.sizeL = sizeL;
  }

  public boolean isDownload() {
    return download;
  }

  public boolean isDownloadable() {
    // contrôle si la photo est téléchargeable et si la date du jour est
    // comprise dans la période de visibilité
    boolean ok;
    Date date = new Date();
    if (beginDownloadDate == null)
      ok = download;
    else {
      if (endDownloadDate == null)
        ok = download && date.after(beginDownloadDate);
      else
        ok = download && date.after(beginDownloadDate)
            && date.before(endDownloadDate);
    }
    return ok;
  }

  public void setDownload(boolean download) {
    this.download = download;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isAlbumLabel() {
    return albumLabel;
  }

  public void setAlbumLabel(boolean albumLabel) {
    this.albumLabel = albumLabel;
  }

  public String getImageMimeType() {
    return imageMimeType;
  }

  public void setImageMimeType(String imageMimeType) {
    this.imageMimeType = imageMimeType;
  }

  public String getImageName() {
    return imageName;
  }

  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public long getImageSize() {
    return imageSize;
  }

  public void setImageSize(long imageSize) {
    this.imageSize = imageSize;
  }

  public PhotoPK getPhotoPK() {
    return photoPK;
  }

  public void setPhotoPK(PhotoPK photoPK) {
    this.photoPK = photoPK;
  }

  public String toString() {
    return "(pk = " + getPhotoPK().toString() + ", name = " + getTitle() + ")";
  }

  public boolean equals(Object o) {
    if (o instanceof PhotoDetail) {
      PhotoDetail anotherPhoto = (PhotoDetail) o;
      return this.photoPK.equals(anotherPhoto.getPhotoPK());
    }
    return false;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public String getUpdateId() {
    return updateId;
  }

  public void setUpdateId(String updateId) {
    this.updateId = updateId;
  }

  public String getUpdateName() {
    return updateName;
  }

  public void setUpdateName(String updateName) {
    this.updateName = updateName;
  }

  public String getName() {
    return getTitle();
  }

  public String getURL() {
    return "searchResult?Type=Photo&Id=" + getId();
  }

  public String getId() {
    return getPhotoPK().getId();
  }

  public String getDate() {
    if (getUpdateDate() != null)
      return DateUtil.date2SQLDate(getUpdateDate());

    return getSilverCreationDate();
  }

  public String getSilverCreationDate() {
    return DateUtil.date2SQLDate(getCreationDate());
  }

  public String getPermalink() {
    if (URLManager.displayUniversalLinks())
      return URLManager.getApplicationURL() + "/Image/" + getId();

    return null;
  }

  public void setPermalink(String permalink) {
    this.permalink = permalink;
  }

  public void addMetaData(MetaData data) {
    metaData.put(data.getProperty(), data);
  }

  public MetaData getMetaData(String property) {
    return (MetaData) metaData.get(property);
  }

  public Collection<String> getMetaDataProperties() {
    //return metaData.keySet();
    Collection<MetaData> values = metaData.values();
    Collection<String> properties = new ArrayList<String>();
    for (MetaData meta : values) {
      if (meta != null) {
        properties.add(meta.getProperty());
      }
    }
    return properties;
  }

  public Date getBeginDownloadDate() {
    return beginDownloadDate;
  }

  public void setBeginDownloadDate(Date beginDownloadDate) {
    this.beginDownloadDate = beginDownloadDate;
  }

  public Date getEndDownloadDate() {
    return endDownloadDate;
  }

  public void setEndDownloadDate(Date endDownloadDate) {
    this.endDownloadDate = endDownloadDate;
  }

  public String getDescription(String language) {
    return getDescription();
  }

  public String getName(String language) {
    return getName();
  }

  public Iterator getLanguages() {
    return null;
  }

  public boolean isVisible(Date today) {
    boolean result = false;
    if (!StringUtil.isDefined(String.valueOf(beginDate))
        && !StringUtil.isDefined(String.valueOf(endDate))) {
      result = true;
    } else {
      if (StringUtil.isDefined(String.valueOf(beginDate))
          && !StringUtil.isDefined(String.valueOf(endDate))) {
        result = beginDate.compareTo(today) <= 0;
      }
      if (!StringUtil.isDefined(String.valueOf(beginDate))
          && StringUtil.isDefined(String.valueOf(endDate))) {
        result = endDate.compareTo(today) >= 0;
      }
      if (StringUtil.isDefined(String.valueOf(beginDate))
          && StringUtil.isDefined(String.valueOf(endDate))) {
        result = beginDate.compareTo(today) <= 0
            && endDate.compareTo(today) >= 0;
      }
    }
    return result;
  }

  /**
   * Get url to access photo from a web site.
   * 
   * @param size  the expecting size of photo (tiny, small, normal, preview, original)
   * 
   * @return the url
   */
  public String getWebURL(String size) {
    PhotoSize photoSize = PhotoSize.get(size);
    
    return getWebURL(photoSize);
  }
   
  /**
   * Get url to access photo from a web site.
   * 
   * @param size  the expecting size of photo
   * 
   * @return the url
   */
  public String getWebURL(PhotoSize size) {
    String idPhoto = photoPK.getId();
    String path = "image" + idPhoto;    
    String name = getImageName();
    if (name != null)
    {
      name = (size.getPrefix().equals(".jpg")) ? name : (getId() + size.getPrefix());
      return FileServerUtils.getWebUrl(photoPK.getSpaceId(), photoPK.getInstanceId(), name, name, getImageMimeType(), path);
    }
    
    return null;
  }
}