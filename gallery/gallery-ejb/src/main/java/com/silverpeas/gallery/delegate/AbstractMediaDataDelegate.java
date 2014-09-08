/*
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
package com.silverpeas.gallery.delegate;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.constant.StreamingProvider;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.Streaming;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.date.Period;
import org.silverpeas.servlet.FileUploadUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractMediaDataDelegate {

  private final MediaType mediaType;
  private final boolean skipEmptyValues;
  private final String language;
  private HeaderData headerData = null;
  private RecordSet recordSet = null;
  private Form form = null;
  private final List<FileItem> parameters;
  private final String albumId;

  /**
   * Default constructor
   * @param mediaType
   * @param language
   * @param albumId
   * @param parameters
   */
  public AbstractMediaDataDelegate(final MediaType mediaType, final String language,
      final String albumId, final List<FileItem> parameters) {
    this(mediaType, language, albumId, parameters, true);
  }

  /**
   * Default constructor
   * @param mediaType
   * @param language
   * @param albumId
   * @param parameters
   * @param skipEmptyValues
   */
  public AbstractMediaDataDelegate(final MediaType mediaType, final String language,
      final String albumId, final List<FileItem> parameters, final boolean skipEmptyValues) {
    this.mediaType = mediaType;
    this.language = language;
    this.skipEmptyValues = skipEmptyValues;
    this.albumId = albumId;
    this.parameters = parameters;
  }

  protected MediaType getMediaType() {
    return mediaType;
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
   * @param media
   */
  public void updateHeader(final Media media) {
    if (!skipEmptyValues && media.getType().isStreaming()) {
      StreamingProvider streamingProvider =
          StreamingProvider.fromUrl(getHeaderData().getHompageUrl());
      if (streamingProvider.isUnknown()) {
        throw new GalleryRuntimeException("AbstractMediaDelegate.updateHeader",
            SilverpeasRuntimeException.ERROR,
            "streaming homepage URL must be defined and supported");
      }
      Streaming streaming = media.getStreaming();
      streaming.setHomepageUrl(getHeaderData().getHompageUrl());
      streaming.setProvider(streamingProvider);
    }
    if (!skipEmptyValues || StringUtil.isDefined(getHeaderData().getTitle())) {
      media.setTitle(getHeaderData().getTitle());
    }
    if (!skipEmptyValues || StringUtil.isDefined(getHeaderData().getDescription())) {
      media.setDescription(getHeaderData().getDescription());
    }
    if (!skipEmptyValues || StringUtil.isDefined(getHeaderData().getAuthor())) {
      media.setAuthor(getHeaderData().getAuthor());
    }
    if (!skipEmptyValues || StringUtil.isDefined(getHeaderData().getKeyWord())) {
      media.setKeyWord(getHeaderData().getKeyWord());
    }
    if (!skipEmptyValues || getHeaderData().getBeginVisibilityDate() != null) {
      media.setVisibilityPeriod(Period
          .getPeriodWithUndefinedIfNull(getHeaderData().getBeginVisibilityDate(),
              media.getVisibilityPeriod().getEndDate()));
    }
    if (!skipEmptyValues || getHeaderData().getEndVisibilityDate() != null) {
      media.setVisibilityPeriod(Period
          .getPeriodWithUndefinedIfNull(media.getVisibilityPeriod().getBeginDate(),
              getHeaderData().getEndVisibilityDate()));
    }
    if (media instanceof InternalMedia) {
      if (!skipEmptyValues || getHeaderData().getBeginDownloadDate() != null) {
        ((InternalMedia) media).setDownloadPeriod(Period
            .getPeriodWithUndefinedIfNull(getHeaderData().getBeginDownloadDate(),
                ((InternalMedia) media).getDownloadPeriod().getEndDate()));
      }
      if (!skipEmptyValues || getHeaderData().getEndDownloadDate() != null) {
        ((InternalMedia) media).setDownloadPeriod(Period.getPeriodWithUndefinedIfNull(
            ((InternalMedia) media).getDownloadPeriod().getBeginDate(),
            getHeaderData().getEndDownloadDate()));
      }
      if (!skipEmptyValues || getHeaderData().isDownloadAuthorized()) {
        ((InternalMedia) media).setDownloadAuthorized(getHeaderData().isDownloadAuthorized());
      }
    }
  }

  /**
   * Perform a form update
   * @param pagesContext
   */
  public void updateForm(final String mediaId, final PagesContext pagesContext) throws Exception {
    final RecordSet set = recordSet;
    DataRecord data = set.getRecord(mediaId);
    if (data == null) {
      data = set.getEmptyRecord();
      data.setId(mediaId);
    }
    form.update(parameters, data, pagesContext);
    set.save(data);
  }

  /**
   * Media header data
   * @author Yohann Chastagnier
   */
  public class HeaderData {

    private String hompageUrl = null;
    private String title = null;
    private String description = null;
    private String author = null;
    private String keyWord = null;
    private Date beginVisibilityDate = null;
    private Date endVisibilityDate = null;
    private boolean downloadAuthorized = false;
    private Date beginDownloadDate = null;
    private Date endDownloadDate = null;
    private List<PdcPosition> pdcPositions = null;

    private String getHompageUrl() {
      return hompageUrl;
    }

    public void setHompageUrl(final String hompageUrl) {
      this.hompageUrl = hompageUrl;
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

    private Date getBeginVisibilityDate() {
      return beginVisibilityDate;
    }

    public void setBeginVisibilityDate(final String beginVisibilityDate) throws ParseException {
      this.beginVisibilityDate = stringToDate(beginVisibilityDate);
    }

    private Date getEndVisibilityDate() {
      return endVisibilityDate;
    }

    public void setEndVisibilityDate(final String endVisibilityDate) throws ParseException {
      this.endVisibilityDate = stringToDate(endVisibilityDate);
    }

    private boolean isDownloadAuthorized() {
      return downloadAuthorized;
    }

    public void setDownloadAuthorized(final String downloadAuthorized) {
      setDownloadAuthorized(StringUtil.getBooleanValue(downloadAuthorized));
    }

    public void setDownloadAuthorized(final boolean download) {
      this.downloadAuthorized = download;
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

    public List<PdcPosition> getPdcPositions() {
      return pdcPositions;
    }

    public void setPdcPositions(List<PdcPosition> pdcPositions) {
      this.pdcPositions = pdcPositions;
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
   * Gets the file from parameters
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