/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.delegate;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.webactiv.util.DateUtil;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractPhotoDataDelegate {

  private final boolean skipEmptyValues;
  private final String language;
  private HeaderData headerData = null;
  private RecordSet recordSet = null;
  private Form form = null;
  private final List<FileItem> parameters;
  private final String albumId;

  /**
   * Default constructor
   * @param language
   * @param albumId
   * @param parameters
   */
  public AbstractPhotoDataDelegate(final String language, final String albumId,
      final List<FileItem> parameters) {
    this(language, albumId, parameters, true);
  }

  /**
   * Default constructor
   * @param language
   * @param albumId
   * @param parameters
   * @param skipEmptyValues
   */
  public AbstractPhotoDataDelegate(final String language, final String albumId,
      final List<FileItem> parameters, final boolean skipEmptyValues) {
    this.language = language;
    this.skipEmptyValues = skipEmptyValues;
    this.albumId = albumId;
    this.parameters = parameters;
  }

  /**
   * Checks if header data exists
   * @return
   */
  public boolean isHeaderData() {
    return headerData != null;
  }

  /**
   * Get the photo header data
   * @return
   */
  public HeaderData getHeaderData() {
    if (headerData == null) {
      headerData = new HeaderData();
    }
    return headerData;
  }

  /**
   * Checks if a form exists
   * @return
   */
  public boolean isForm() {
    return recordSet != null && form != null && parameters != null;
  }

  /**
   * Set a form
   */
  public void setForm(final RecordSet recordSet, final Form form) {
    this.recordSet = recordSet;
    this.form = form;
  }

  /**
   * Perform a header data update
   * @param photo
   */
  public void updateHeader(final PhotoDetail photo) {
    if (getHeaderData().getAlbumLabel() != null) {
      photo.setAlbumLabel(getHeaderData().getAlbumLabel());
    }
    if (!skipEmptyValues || StringUtil.isDefined(getHeaderData().getTitle())) {
      photo.setTitle(getHeaderData().getTitle());
    }
    if (!skipEmptyValues || StringUtil.isDefined(getHeaderData().getDescription())) {
      photo.setDescription(getHeaderData().getDescription());
    }
    if (!skipEmptyValues || StringUtil.isDefined(getHeaderData().getAuthor())) {
      photo.setAuthor(getHeaderData().getAuthor());
    }
    if (!skipEmptyValues || StringUtil.isDefined(getHeaderData().getKeyWord())) {
      photo.setKeyWord(getHeaderData().getKeyWord());
    }
    if (!skipEmptyValues || getHeaderData().isDownload()) {
      photo.setDownload(getHeaderData().isDownload());
    }
    if (!skipEmptyValues || getHeaderData().getBeginDownloadDate() != null) {
      photo.setBeginDownloadDate(getHeaderData().getBeginDownloadDate());
    }
    if (!skipEmptyValues || getHeaderData().getEndDownloadDate() != null) {
      photo.setEndDownloadDate(getHeaderData().getEndDownloadDate());
    }
    if (!skipEmptyValues || getHeaderData().getBeginDate() != null) {
      photo.setBeginDate(getHeaderData().getBeginDate());
    }
    if (!skipEmptyValues || getHeaderData().getEndDate() != null) {
      photo.setEndDate(getHeaderData().getEndDate());
    }
  }

  /**
   * Perform a form update
   * @param pagesContext
   */
  public void updateForm(final String photoId, final PagesContext pagesContext) throws Exception {
    final RecordSet set = recordSet;
    DataRecord data = set.getRecord(photoId);
    if (data == null) {
      data = set.getEmptyRecord();
      data.setId(photoId);
    }
    form.update(parameters, data, pagesContext);
    set.save(data);
  }

  /**
   * Photo header data
   * @author Yohann Chastagnier
   */
  public class HeaderData {

    private Boolean albumLabel = null;
    private String title = null;
    private String description = null;
    private String author = null;
    private String keyWord = null;
    private boolean download = false;
    private Date beginDownloadDate = null;
    private Date endDownloadDate = null;
    private Date beginDate = null;
    private Date endDate = null;

    public Boolean getAlbumLabel() {
      return albumLabel;
    }

    public void setAlbumLabel(final String albumLabel) {
      this.albumLabel = albumLabel != null && Boolean.valueOf(albumLabel);
    }

    private String getTitle() {
      return (title == null) ? "" : title;
    }

    public void setTitle(final String title) {
      this.title = title;
    }

    private String getDescription() {
      return description;
    }

    public void setDescription(final String description) {
      this.description = description;
    }

    private String getAuthor() {
      return author;
    }

    public void setAuthor(final String author) {
      this.author = author;
    }

    private String getKeyWord() {
      return keyWord;
    }

    public void setKeyWord(final String keyWord) {
      this.keyWord = keyWord;
    }

    private boolean isDownload() {
      return download;
    }

    public void setDownload(final String download) {
      this.download = download != null && Boolean.valueOf(download);
    }

    public void setDownload(final boolean download) {
      this.download = download;
    }

    private Date getBeginDownloadDate() {
      return beginDownloadDate;
    }

    public void setBeginDownloadDate(final String beginDownloadDate) throws ParseException {
      this.beginDownloadDate = stringToDate(beginDownloadDate);
    }

    private Date getEndDownloadDate() {
      return endDownloadDate;
    }

    public void setEndDownloadDate(final String endDownloadDate) throws ParseException {
      this.endDownloadDate = stringToDate(endDownloadDate);
    }

    private Date getBeginDate() {
      return beginDate;
    }

    public void setBeginDate(final String beginDate) throws ParseException {
      this.beginDate = stringToDate(beginDate);
    }

    private Date getEndDate() {
      return endDate;
    }

    public void setEndDate(final String endDate) throws ParseException {
      this.endDate = stringToDate(endDate);
    }
  }

  /**
   * Internal tool
   * @param stringDate
   * @return
   * @throws ParseException
   */
  private Date stringToDate(final String stringDate) throws ParseException {
    Date date = null;
    if (stringDate != null && StringUtil.isDefined(stringDate.trim())) {
      date = DateUtil.stringToDate(stringDate, language);
    }
    return date;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @return the albumId
   */
  public String getAlbumId() {
    return albumId;
  }

  /**
   * Gets the photo from parameters
   * @return
   */
  public FileItem getFileItem() {
    return FileUploadUtil.getFile(parameters, "WAIMGVAR0");
  }

  /**
   * @return the skipEmptyValues
   */
  public boolean isSkipEmptyValues() {
    return skipEmptyValues;
  }
}