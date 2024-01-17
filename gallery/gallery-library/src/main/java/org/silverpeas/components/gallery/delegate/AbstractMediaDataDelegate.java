/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.delegate;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.Streaming;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.media.streaming.StreamingProvider;
import org.silverpeas.core.media.streaming.StreamingProvidersRegistry;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUploadUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

  public AbstractMediaDataDelegate(final MediaType mediaType, final String language,
      final String albumId, final List<FileItem> parameters) {
    this(mediaType, language, albumId, parameters, true);
  }

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
   * @return true if the header data is set. False otherwise.
   */
  public boolean isHeaderData() {
    return headerData != null;
  }

  /**
   * Get the photo header data
   * @return a {@link HeaderData} instance.
   */
  public HeaderData getHeaderData() {
    if (headerData == null) {
      headerData = new HeaderData();
    }
    return headerData;
  }

  /**
   * Checks if a form exists
   * @return true if there is both a form set and the corresponding parameters to that form.
   * False otherwise.
   */
  public boolean isForm() {
    return recordSet != null && form != null && parameters != null;
  }

  /**
   * Set a form.
   * @param recordSet the set of form's records
   * @param form the form definition.
   */
  public void setForm(final RecordSet recordSet, final Form form) {
    this.recordSet = recordSet;
    this.form = form;
  }

  /**
   * Perform a header data update
   * @param media the media to update with the header data.
   */
  public void updateHeader(final Media media) {
    if (!skipEmptyValues && media.getType().isStreaming()) {
      final Optional<StreamingProvider> streamingProvider =
          StreamingProvidersRegistry.get().getFromUrl(getHeaderData().getHomepageUrl());
      if (streamingProvider.isEmpty()) {
        throw new GalleryRuntimeException("Streaming homepage URL must be defined and supported");
      }
      Streaming streaming = media.getStreaming();
      streaming.setHomepageUrl(getHeaderData().getHomepageUrl());
      streaming.setProvider(streamingProvider.get());
    }
    updateMedia(media);
  }

  private void updateMedia(final Media media) {
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
    updateMediaVisibilityPeriod(media);
    updateMediaDownloadPeriod(media);
  }

  private void updateMediaDownloadPeriod(final Media media) {
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

  private void updateMediaVisibilityPeriod(final Media media) {
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
  }

  /**
   * Perform a form update
   * @param mediaId the unique identifier of the media for which the form has to be updated
   * @param pagesContext pages context of an updated form
   */
  public void updateForm(final String mediaId, final PagesContext pagesContext) throws
      FormException {
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

    private String homepageUrl = null;
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

    private Date stringToDate(final String stringDate) throws ParseException {
      Date date = null;
      if (stringDate != null && StringUtil.isDefined(stringDate.trim())) {
        date = DateUtil.stringToDate(stringDate, language);
      }
      return date;
    }

    private String getHomepageUrl() {
      return homepageUrl;
    }

    public void setHomepageUrl(final String homepageUrl) {
      this.homepageUrl = homepageUrl;
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
   * @return a {@link FileItem} object
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